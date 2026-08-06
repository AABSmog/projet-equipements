package proj.equipment.admin

import proj.equipment.Affectation
import proj.equipment.Equipement
import proj.equipment.EtatEquipement
import proj.equipment.Personnel

class AffectationController {

    static namespace = "admin"

    def list() {
        def q = params.q
        def results = q ? Affectation.createCriteria().list {
            or {
                personnel { ilike("nom", "%${q}%") }
                ilike("infoEquipement", "%${q}%")
                and {
                    isNotNull("equipement")
                    equipement { ilike("numeroSerie", "%${q}%") }
                }
            }
        } : Affectation.list(sort: "dateAffectation", order: "desc")
        [affectationList: results]
    }

    def affecter() {
        def equipement = Equipement.get(params.equipementId)
        def personnel = Personnel.get(params.personnelId)
        if (equipement && personnel && equipement.etat == EtatEquipement.DISPONIBLE) {
            Affectation.withTransaction { status ->
                def affectation = new Affectation(
                    equipement: equipement,
                    personnel: personnel,
                    dateAffectation: new Date()
                )
                affectation.save(flush: true)
                equipement.etat = EtatEquipement.AFFECTE
                equipement.save(flush: true)
            }
            flash.success = "Equipement affecte a ${personnel.prenom} ${personnel.nom}"
        } else {
            flash.error = "Impossible d'affecter cet equipement"
        }
        redirect(action: "list")
    }

    def retour() {
        def affectation = Affectation.get(params.id)
        if (affectation) {
            Affectation.withTransaction { status ->
                affectation.dateRetour = new Date()
                affectation.raisonRetour = params.raisonRetour
                affectation.save(flush: true)
                if (affectation.equipement) {
                    affectation.equipement.etat = EtatEquipement.DISPONIBLE
                    affectation.equipement.save(flush: true)
                }
            }
            flash.success = "Retour enregistre"
        }
        redirect(action: "list")
    }
}
