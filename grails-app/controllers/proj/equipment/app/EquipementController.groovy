package proj.equipment.app

import proj.equipment.Equipement
import proj.equipment.Affectation
import proj.equipment.Signalement
import proj.equipment.EtatEquipement
import proj.equipment.TypeEquipement

class EquipementController {

    static namespace = "app"

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
        def affectation = Affectation.findByEquipementAndPersonnelAndDateRetourIsNull(equipement, session.user)
        if (!affectation) {
            flash.error = "Aucune affectation active pour cet equipement"
            redirect(action: "list")
            return
        }
        Affectation.withTransaction { status ->
            affectation.dateRetour = new Date()
            affectation.raisonRetour = params.raisonRetour ?: "Retour volontaire"
            affectation.save(flush: true)
            equipement.etat = EtatEquipement.DISPONIBLE
            equipement.save(flush: true)
        }
        flash.success = "Equipement retourne avec succes"
        redirect(action: "list")
    }
}
