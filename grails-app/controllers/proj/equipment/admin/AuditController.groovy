package proj.equipment.admin

import proj.equipment.AuditLog

class AuditController {

    static namespace = "admin"

    def auditService

    def list() {
        int max = Math.min((params.max as Integer) ?: 20, 200)
        int offset = Math.max(((params.offset as Integer) ?: 0).toInteger(), 0)
        def q = params.q
        def results = AuditLog.createCriteria().list(max: max, offset: offset) {
            if (q) {
                or {
                    ilike("utilisateur", "%${q}%")
                    ilike("action", "%${q}%")
                    ilike("cible", "%${q}%")
                    ilike("details", "%${q}%")
                }
            }
            order("dateCreated", "desc")
        }
        [auditList: results, total: results.totalCount, max: max, offset: offset]
    }
}