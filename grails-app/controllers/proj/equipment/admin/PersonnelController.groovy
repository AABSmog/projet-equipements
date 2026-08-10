package proj.equipment.admin

import proj.equipment.Personnel
import proj.equipment.RolePersonnel
import proj.equipment.Affectation
import proj.equipment.Signalement
import grails.gorm.transactions.Transactional

@Transactional
class PersonnelController {

    static namespace = "admin"

    def validationMessagesService
    def auditService

    def list() {
        def q = params.q
        int max = Math.min((params.max as Integer) ?: 10, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def results = Personnel.createCriteria().list(max: max, offset: offset) {
            if (q) {
                or {
                    ilike("nom", "%${q}%")
                    ilike("prenom", "%${q}%")
                    ilike("email", "%${q}%")
                }
            }
            order("nom", "asc")
        }
        [personnelList: results, total: results.totalCount, max: max, offset: offset]
    }

    def create() {
        [personnel: new Personnel(params)]
    }

    def save() {
        def personnel = new Personnel(params)
        if (personnel.save(flush: true)) {
            auditService.log(session.user, "CREATE", "Personnel", personnel.id, personnel.email)
            flash.success = "Personnel cree"
            redirect(action: "list")
        } else {
            flash.error = validationMessagesService.message(personnel) ?: "Erreur lors de la creation"
            render(view: "create", model: [personnel: personnel])
        }
    }

    def edit() {
        def personnel = Personnel.get(params.id)
        if (!personnel) {
            flash.error = "Personnel introuvable"
            redirect(action: "list")
            return
        }
        [personnel: personnel]
    }

    def update() {
        def personnel = Personnel.get(params.id)
        if (!personnel) {
            flash.error = "Personnel introuvable"
            redirect(action: "list")
            return
        }
        personnel.properties['nom'] = params.nom
        personnel.properties['prenom'] = params.prenom
        personnel.properties['email'] = params.email
        personnel.properties['role'] = params.role
        if (params.motDePasse) {
            personnel.motDePasse = params.motDePasse
        }
        if (personnel.save(flush: true)) {
            auditService.log(session.user, "UPDATE", "Personnel", personnel.id, personnel.email)
            flash.success = "Personnel mis a jour"
            redirect(action: "list")
        } else {
            flash.error = validationMessagesService.message(personnel) ?: "Erreur lors de la mise a jour"
            render(view: "edit", model: [personnel: personnel])
        }
    }

    def delete() {
        def personnel = Personnel.get(params.id)
        if (personnel) {
            if (personnel.id == session.user?.id) {
                flash.error = "Impossible de supprimer votre propre compte"
                redirect(action: "list")
                return
            }
            def nbAffectations = Affectation.countByPersonnel(personnel)
            def nbSignalements = Signalement.countByPersonnel(personnel)
            if (nbAffectations > 0 || nbSignalements > 0) {
                flash.error = "Suppression impossible : historique d'affectations ou de signalements existant"
                redirect(action: "list")
                return
            }
            personnel.delete(flush: true)
            auditService.log(session.user, "DELETE", "Personnel", personnel.id, personnel.email)
            flash.success = "Personnel supprime"
        }
        redirect(action: "list")
    }
}
