package proj.equipment

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class AuditServiceSpec extends Specification implements ServiceUnitTest<AuditService>, DataTest {

    def setupSpec() {
        mockDomains AuditLog, Personnel
    }

    void "une action administrateur laisse une trace d audit"() {
        given: "un operateur"
        def admin = new Personnel(nom: "Diop", prenom: "Mamadou", email: "admin@example.com",
                motDePasse: "Admin123", role: RolePersonnel.ADMIN).save(flush: true, failOnError: true)

        when: "une action est journalisee"
        service.log(admin, "CREATE", "Equipement", 42L, "Ordinateur - SN-001")

        then: "la trace est enregistree"
        AuditLog.count() == 1
        def trace = AuditLog.list()[0]
        trace.action == "CREATE"
        trace.cible == "Equipement"
        trace.cibleId == 42L
        trace.utilisateur == "admin@example.com"
    }

    void "sans operateur, la trace est attribuee au systeme"() {
        when:
        service.log(null, "LOGIN", "Personnel", 1L)

        then:
        AuditLog.count() == 1
        AuditLog.list()[0].utilisateur == "systeme"
    }

    void "les traces recentes sont triees de la plus recente a la plus ancienne"() {
        given:
        service.log(null, "CREATE", "Equipement", 1L, "premier")
        Thread.sleep(20)
        service.log(null, "UPDATE", "Equipement", 1L, "second")

        when:
        def recent = service.recent(10)

        then:
        recent.size() == 2
        recent[0].details == "second"
        recent[1].details == "premier"
    }
}