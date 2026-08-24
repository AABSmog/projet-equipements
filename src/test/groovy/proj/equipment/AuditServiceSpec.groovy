package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import proj.equipment.domain.RolePersonnel
import proj.equipment.service.AuditService
import spock.lang.Specification
import jakarta.transaction.Transactional

@MicronautTest
class AuditServiceSpec extends Specification {

    @Inject AuditService auditService
    @Inject EntityManager em

    @Transactional
    void "une action administrateur laisse une trace d audit"() {
        given:
        def admin = new Personnel(nom: "Admin", prenom: "Test", email: "audit-${UUID.randomUUID()}@example.com", motDePasse: "Admin123", role: RolePersonnel.ADMIN)
        em.persist(admin)
        em.flush()

        when:
        auditService.log(admin, "CREATE", "Equipement", 42L, "detail")

        then:
        def logs = em.createQuery("from AuditLog where cibleId = :id", proj.equipment.domain.AuditLog).setParameter("id", 42L).resultList
        logs.size() == 1
        logs[0].action == "CREATE"
        logs[0].utilisateur == admin.email
    }

    @Transactional
    void "sans operateur, la trace est attribuee au systeme"() {
        when:
        auditService.log(null, "LOGIN", "Personnel", 1L, "connexion")

        then:
        def logs = em.createQuery("from AuditLog where cibleId = :id", proj.equipment.domain.AuditLog).setParameter("id", 1L).resultList
        logs[0].utilisateur == "systeme"
    }

    @Transactional
    void "les traces recentes sont triees de la plus recente a la plus ancienne"() {
        given:
        def p = new Personnel(nom: "A", prenom: "B", email: "audit2-${UUID.randomUUID()}@example.com", motDePasse: "Admin123")
        em.persist(p)
        em.flush()
        auditService.log(p, "ACTION1", "Test", 1L, "first")
        Thread.sleep(10)
        auditService.log(p, "ACTION2", "Test", 2L, "second")
        em.flush()

        when:
        def recent = auditService.recent(10)

        then:
        recent[0].action == "ACTION2"
        recent[1].action == "ACTION1"
    }
}
