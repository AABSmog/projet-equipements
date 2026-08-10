package proj.equipment.app

import proj.equipment.Equipement
import proj.equipment.Affectation
import proj.equipment.Signalement
import proj.equipment.TypeEquipement

class EquipementController {

    static namespace = "app"

    def affectationService

    def list() {
        def user = session.user
        def affectations = Affectation.findAllByPersonnelAndDateRetourIsNull(user, [sort: "dateAffectation", order: "desc"])
        [affectationList: affectations]
    }

    def show() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def signalements = Signalement.findAllByEquipement(equipement, [sort: "dateCreated", order: "desc"])
        [equipement: equipement, signalementList: signalements]
    }

    def retour() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def result = affectationService.restituer(equipement, session.user, params.raisonRetour)
        if (result.success) {
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }
}
