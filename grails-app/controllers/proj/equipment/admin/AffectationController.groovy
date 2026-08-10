package proj.equipment.admin

import proj.equipment.Affectation
import proj.equipment.Equipement
import proj.equipment.Personnel

class AffectationController {

    static namespace = "admin"

    def affectationService

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

    def historique() {
        def q = params.q
        def results = q ? Affectation.createCriteria().list {
            or {
                personnel { ilike("nom", "%${q}%") }
                attribuePar { ilike("nom", "%${q}%") }
                ilike("infoEquipement", "%${q}%")
                and {
                    isNotNull("equipement")
                    equipement { ilike("numeroSerie", "%${q}%") }
                }
            }
            order("dateAffectation", "desc")
        } : Affectation.list(sort: "dateAffectation", order: "desc")
        [affectationList: results]
    }

    def affecter() {
        def equipement = Equipement.get(params.equipementId)
        def personnel = Personnel.get(params.personnelId)
        def result = affectationService.attribuer(equipement, personnel, session.user)
        if (result.success) {
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
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }
}
