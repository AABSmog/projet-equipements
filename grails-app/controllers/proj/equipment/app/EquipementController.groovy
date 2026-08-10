package proj.equipment.app

import proj.equipment.Equipement
import proj.equipment.Affectation
import proj.equipment.Signalement
import proj.equipment.TypeEquipement

class EquipementController {

    static namespace = "app"

    def affectationService
    def auditService

    def list() {
        def user = session.user
        int max = Math.min((params.max as Integer) ?: 10, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def results = Affectation.createCriteria().list(max: max, offset: offset) {
            eq("personnel", user)
            isNull("dateRetour")
            fetchMode('equipement', org.hibernate.FetchMode.JOIN)
            order("dateAffectation", "desc")
        }
        [affectationList: results, total: results.totalCount, max: max, offset: offset]
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
            auditService.log(session.user, "RETOUR", "Equipement", equipement.id, params.raisonRetour)
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }
}
