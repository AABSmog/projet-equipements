package proj.equipment.admin

import grails.converters.JSON
import proj.equipment.Affectation
import proj.equipment.Equipement
import proj.equipment.EtatEquipement
import proj.equipment.Personnel
import proj.equipment.TypeEquipement
import org.hibernate.FetchMode
import org.hibernate.sql.JoinType

class AffectationController {

    static namespace = "admin"

    def affectationService
    def auditService

    private Map listeAvecFetch(int max, int offset, String q, Closure tri) {
        def results = Affectation.createCriteria().list(max: max, offset: offset) {
            fetchMode('personnel', FetchMode.JOIN)
            fetchMode('attribuePar', FetchMode.JOIN)
            fetchMode('equipement', FetchMode.JOIN)
            if (q) {
                def pattern = "%${q}%"
                createAlias("personnel", "p")
                createAlias("attribuePar", "ap", JoinType.LEFT_OUTER_JOIN)
                or {
                    ilike("p.nom", pattern)
                    ilike("p.prenom", pattern)
                    ilike("ap.nom", pattern)
                    ilike("ap.prenom", pattern)
                    ilike("infoEquipement", pattern)
                    equipement { ilike("numeroSerie", pattern) }
                    equipement { type { ilike("nom", pattern) } }
                }
            }
            tri.call(delegate)
        }
        [affectationList: results, total: results.totalCount, max: max, offset: offset]
    }

    def list() {
        int max = Math.min((params.max as Integer) ?: 10, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def q = params.q
        def model = [:]
        model.putAll(listeAvecFetch(max, offset, q, { c -> c.order("dateAffectation", "desc") }))
        model.typeEquipementList = TypeEquipement.list(sort: "nom")
        model
    }

    def historique() {
        int max = Math.min((params.max as Integer) ?: 10, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def q = params.q
        def model = [:]
        def res = listeAvecFetch(max, offset, q, { c -> c.order("dateAffectation", "desc") })
        model.putAll(res)
        model
    }

    def affecter() {
        def equipement = Equipement.get(params.equipementId)
        def personnel = Personnel.get(params.personnelId)
        def result = affectationService.attribuer(equipement, personnel, session.user)
        if (result.success) {
            auditService.log(session.user, "ATTRIBUTION", "Affectation", null, "${equipement?.numeroSerie} -> ${personnel?.email}")
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }

    def retour() {
        def affectation = Affectation.get(params.id)
        def result = affectationService.restituer(affectation, params.raisonRetour)
        if (result.success) {
            auditService.log(session.user, "RETOUR", "Affectation", affectation?.id, params.raisonRetour)
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "historique", params: params.q ? [q: params.q] : [:])
    }

    def rechercherEquipements() {
        String q = params.q?.toString()?.trim()
        Long typeId = params.typeId?.isLong() ? params.typeId.toLong() : null
        def results = Equipement.createCriteria().list(max: 20) {
            eq("etat", EtatEquipement.DISPONIBLE)
            if (typeId || q) {
                createAlias("type", "t")
            }
            if (typeId) {
                eq("t.id", typeId)
            }
            if (q) {
                def pattern = "%${q}%"
                or {
                    ilike("numeroSerie", pattern)
                    ilike("description", pattern)
                    ilike("t.nom", pattern)
                }
            }
            order("numeroSerie", "asc")
        }
        render(results.collect {
            [id: it.id, label: "${it.type?.nom ?: ''} - ${it.numeroSerie}".replaceAll('^ - ', ''), numeroSerie: it.numeroSerie]
        } as JSON)
    }

    def rechercherPersonnel() {
        String q = params.q?.toString()?.trim()
        def results = Personnel.createCriteria().list(max: 20) {
            if (q) {
                def pattern = "%${q}%"
                or {
                    ilike("nom", pattern)
                    ilike("prenom", pattern)
                }
            }
            order("nom", "asc")
            order("prenom", "asc")
        }
        render(results.collect {
            [id: it.id, label: "${it.prenom} ${it.nom}"]
        } as JSON)
    }
}
