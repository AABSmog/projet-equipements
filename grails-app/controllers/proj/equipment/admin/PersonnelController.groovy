package proj.equipment.admin

import proj.equipment.Personnel
import proj.equipment.RolePersonnel
import grails.gorm.transactions.Transactional

@Transactional
class PersonnelController {

    static namespace = "admin"

    def validationMessagesService

    def list() {
        def q = params.q
        def results = q ? Personnel.createCriteria().list {
            or {
                ilike("nom", "%${q}%")
                ilike("prenom", "%${q}%")
                ilike("email", "%${q}%")
            }
        } : Personnel.list(sort: "nom")
        [personnelList: results]
    }

    def create() {
        [personnel: new Personnel(params)]
    }

    def save() {
        def personnel = new Personnel(params)
        if (personnel.save(flush: true)) {
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
            personnel.delete(flush: true)
            flash.success = "Personnel supprime"
        }
        redirect(action: "list")
    }
}
