package proj.equipment.admin

import proj.equipment.Affectation
import proj.equipment.Equipement
import proj.equipment.Personnel
import org.hibernate.FetchMode

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
                or {
                    personnel { ilike("nom", "%${q}%") }
                    attribuePar { ilike("nom", "%${q}%") }
                    ilike("infoEquipement", "%${q}%")
                    and {
                        isNotNull("equipement")
                        equipement { ilike("numeroSerie", "%${q}%") }
                    }
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
        redirect(action: "list")
    }
}
