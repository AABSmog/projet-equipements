package proj.equipment

class AuditLog {

    Date dateCreated
    String utilisateur
    String action
    String cible
    Long cibleId
    String details

    static constraints = {
        utilisateur nullable: true, maxSize: 254
        action blank: false, maxSize: 50
        cible nullable: true, maxSize: 100
        cibleId nullable: true
        details nullable: true, maxSize: 1000
    }

    static mapping = {
        dateCreated column: "date_creation", index: "idx_audit_date"
        cibleId index: "idx_audit_cible"
        sort dateCreated: "desc"
    }

    String toString() { "Audit ${action} ${cible}#${cibleId} par ${utilisateur}" }
}