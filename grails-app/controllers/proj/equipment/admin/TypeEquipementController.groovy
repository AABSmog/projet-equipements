package proj.equipment.admin

import proj.equipment.TypeEquipement
import grails.gorm.transactions.Transactional

@Transactional
class TypeEquipementController {

    static namespace = "admin"

    def index() { redirect(action: "list") }

    def list() {
        def q = params.q
        def results = q ? TypeEquipement.createCriteria().list {
            ilike("nom", "%${q}%")
        } : TypeEquipement.list(sort: "nom")
        [typeEquipementList: results]
    }

    def create() {
        flash.error = "Les types se creent via l'option 'Autre...' dans le formulaire equipement"
        redirect(action: "list")
    }

    def save() {
        flash.error = "Les types se creent via l'option 'Autre...' dans le formulaire equipement"
        redirect(action: "list")
    }

    def edit() {
        flash.error = "Modification de type desactivee"
        redirect(action: "list")
    }

    def update() {
        flash.error = "Modification de type desactivee"
        redirect(action: "list")
    }

    def delete() {
        flash.error = "Suppression de type desactivee"
        redirect(action: "list")
    }
}
