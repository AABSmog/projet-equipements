package proj.equipment.admin

import proj.equipment.Signalement
import grails.gorm.transactions.Transactional
import org.hibernate.FetchMode

@Transactional
class SignalementController {

    static namespace = "admin"

    def auditService

    def list() {
        def q = params.q
        int max = Math.min((params.max as Integer) ?: 10, 100)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def results = Signalement.createCriteria().list(max: max, offset: offset) {
            fetchMode('personnel', FetchMode.JOIN)
            fetchMode('equipement', FetchMode.JOIN)
            if (q) {
                or {
                    ilike("description", "%${q}%")
                    and {
                        isNotNull("equipement")
                        equipement { ilike("numeroSerie", "%${q}%") }
                    }
                }
            }
            order("dateCreated", "desc")
        }
        [signalementList: results, total: results.totalCount, max: max, offset: offset]
    }

    def delete() {
        def signalement = Signalement.get(params.id)
        if (signalement) {
            def info = signalement.personnel?.email
            signalement.delete(flush: true)
            auditService.log(session.user, "DELETE", "Signalement", params.id?.toLong(), info)
            flash.success = "Signalement supprime"
        }
        redirect(action: "list")
    }
}
