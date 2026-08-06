package proj.equipment.admin

import proj.equipment.Signalement
import grails.gorm.transactions.Transactional

@Transactional
class SignalementController {

    static namespace = "admin"

    def list() {
        def q = params.q
        def results = q ? Signalement.createCriteria().list {
            or {
                ilike("description", "%${q}%")
                and {
                    isNotNull("equipement")
                    equipement { ilike("numeroSerie", "%${q}%") }
                }
            }
        } : Signalement.list(sort: "dateCreated", order: "desc")
        [signalementList: results]
    }

    def delete() {
        def signalement = Signalement.get(params.id)
        if (signalement) {
            signalement.delete(flush: true)
            flash.success = "Signalement supprime"
        }
        redirect(action: "list")
    }
}
