package proj.equipment

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j

/**
 * Journal d'audit des actions sensibles (création, modification, suppression,
 * attribution, connexion...). Écrit en base et consultable par l'administrateur.
 */
@Slf4j
@Transactional
class AuditService {

    void log(Personnel operateur, String action, String cible, Long cibleId = null, String details = null) {
        def trace = new AuditLog(
            utilisateur: operateur?.email ?: 'systeme',
            action: action,
            cible: cible,
            cibleId: cibleId,
            details: details
        )
        if (!trace.save(flush: true)) {
            log.warn("Impossible d'ecrire la trace d'audit {} {}", action, cible)
        }
    }

    List<AuditLog> recent(int max = 50) {
        int limite = Math.min(max, 200)
        AuditLog.createCriteria().list(max: limite) {
            order("dateCreated", "desc")
        }
    }
}