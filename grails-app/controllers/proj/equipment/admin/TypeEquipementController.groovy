package proj.equipment.admin

import proj.equipment.TypeEquipement
import grails.gorm.transactions.Transactional

@Transactional
class TypeEquipementController {

    static namespace = "admin"

    def index() { redirect(action: "list") }

    def list() {
        def q = params.q
        int max = Math.min((params.max as Integer) ?: 20, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def results = TypeEquipement.createCriteria().list(max: max, offset: offset) {
            if (q) {
                ilike("nom", "%${q}%")
            }
            order("nom", "asc")
        }
        [typeEquipementList: results, total: results.totalCount, max: max, offset: offset]
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
