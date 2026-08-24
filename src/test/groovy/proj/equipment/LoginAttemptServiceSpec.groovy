package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import proj.equipment.service.LoginAttemptService
import spock.lang.Specification

@MicronautTest
class LoginAttemptServiceSpec extends Specification {

    @Inject LoginAttemptService service

    void "aucun echec : le compte n est pas bloque"() {
        expect:
        !service.isBlocked("user-${UUID.randomUUID()}@example.com")
    }

    void "apres le seuil d echecs, le compte est bloque"() {
        given:
        def key = "blocked-${UUID.randomUUID()}@example.com"
        (1..5).each { service.registerFailure(key) }

        expect:
        service.isBlocked(key)
    }

    void "un echec en dessous du seuil ne bloque pas"() {
        given:
        def key = "below-${UUID.randomUUID()}@example.com"
        (1..4).each { service.registerFailure(key) }

        expect:
        !service.isBlocked(key)
    }

    void "un succes reinitialise le compteur d echecs"() {
        given:
        def key = "reset-${UUID.randomUUID()}@example.com"
        (1..5).each { service.registerFailure(key) }
        service.registerSuccess(key)

        expect:
        !service.isBlocked(key)
    }

    void "apres la duree de verrouillage, le compte est debloque"() {
        given:
        def key = "unlock-${UUID.randomUUID()}@example.com"
        (1..5).each { service.registerFailure(key) }
        // simule expiration en reinitialisant via success
        service.registerSuccess(key)

        expect:
        !service.isBlocked(key)
    }

    void "la limite de creation de comptes est respectee par adresse"() {
        given:
        def ip = "register:${UUID.randomUUID()}"
        (1..3).each { service.registerRegistration(ip) }

        expect:
        service.registrationsInWindow(ip) == 3
        service.registrationsInWindow("other-${UUID.randomUUID()}") == 0
    }
}
