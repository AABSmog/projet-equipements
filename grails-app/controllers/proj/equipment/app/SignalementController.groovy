package proj.equipment.app

import proj.equipment.Signalement
import proj.equipment.Equipement
import proj.equipment.Affectation
import grails.gorm.transactions.Transactional

@Transactional
class SignalementController {

    static namespace = "app"
    static defaultAction = "list"

    def validationMessagesService

    def list() {
        def user = session.user
        [signalementList: Signalement.findAllByPersonnel(user, [sort: "dateCreated", order: "desc"])]
    }

    def create() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(uri: "/app/equipement/list")
            return
        }
        // Vérifier que l'équipement est bien affecté à l'utilisateur
        def affectation = Affectation.findByEquipementAndPersonnelAndDateRetourIsNull(equipement, session.user)
        if (!affectation) {
            flash.error = "Cet equipement ne vous est pas affecte"
            redirect(uri: "/app/equipement/list")
            return
        }
        [equipement: equipement]
    }

    def save() {
        def equipement = Equipement.get(params.equipementId)
        def affectation = Affectation.findByEquipementAndPersonnelAndDateRetourIsNull(equipement, session.user)
        if (!affectation) {
            flash.error = "Cet equipement ne vous est pas affecte"
            redirect(uri: "/app/equipement/list")
            return
        }
        if (!params.description?.trim()) {
            flash.error = "La description est obligatoire."
            render(view: "create", model: [equipement: equipement])
            return
        }
        def typeSignalement
        if (params.type) {
            try {
                typeSignalement = proj.equipment.TypeSignalement.valueOf(params.type)
            } catch (IllegalArgumentException e) {
                flash.error = "Type de signalement invalide"
                render(view: "create", model: [equipement: equipement])
                return
            }
        }
        def signalement = new Signalement(
            equipement: equipement,
            personnel: session.user,
            type: typeSignalement,
            description: params.description
        )
        if (signalement.save(flush: true)) {
            flash.success = "Signalement enregistre"
            redirect(controller: "equipement", action: "show", id: equipement.id, namespace: "app")
        } else {
            flash.error = validationMessagesService.message(signalement) ?: "Erreur lors de l'envoi du signalement"
            render(view: "create", model: [equipement: equipement])
        }
    }
}
