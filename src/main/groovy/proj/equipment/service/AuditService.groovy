package proj.equipment.service

import groovy.util.logging.Slf4j
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.AuditLog
import proj.equipment.domain.Personnel

@Slf4j
@Singleton
class AuditService {

    @PersistenceContext
    EntityManager em

    @Transactional
    void log(Personnel operateur, String action, String cible, Long cibleId = null, String details = null) {
        try {
            AuditLog trace = new AuditLog(
                    utilisateur: operateur?.email ?: 'systeme',
                    action: action,
                    cible: cible,
                    cibleId: cibleId,
                    details: details
            )
            em.persist(trace)
        } catch (Exception e) {
            log.warn("Impossible d'ecrire la trace d'audit {} {} : {}", action, cible, e.message)
        }
    }

    List<AuditLog> recent(int max = 20) {
        em.createQuery("from AuditLog order by dateCreated desc", AuditLog).setMaxResults(max).resultList as List<AuditLog>
    }
}