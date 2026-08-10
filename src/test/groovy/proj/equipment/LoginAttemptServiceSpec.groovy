package proj.equipment

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class LoginAttemptServiceSpec extends Specification implements ServiceUnitTest<LoginAttemptService> {

    def setup() {
        service.maxAttempts = 3
        service.windowSeconds = 900
        service.lockDurationSeconds = 300
        service.maxRegistrations = 2
        service.registrationWindowSeconds = 3600
    }

    void "aucun echec : le compte n est pas bloque"() {
        expect:
        !service.isBlocked("ali@example.com")
    }

    void "apres le seuil d echecs, le compte est bloque"() {
        when: "trois echecs sont enregistres"
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")

        then: "le compte est bloque"
        service.isBlocked("ali@example.com")
    }

    void "un echec en dessous du seuil ne bloque pas"() {
        when: "deux echecs pour un seuil de trois"
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")

        then:
        !service.isBlocked("ali@example.com")
    }

    void "un succes reinitialise le compteur d echecs"() {
        given: "deux echecs sur un seuil de trois"
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")

        when: "une connexion reussit"
        service.registerSuccess("ali@example.com")

        then:
        !service.isBlocked("ali@example.com")
    }

    void "apres la duree de verrouillage, le compte est debloque"() {
        given: "un verrouillage de une seconde atteint"
        service.lockDurationSeconds = 1
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")
        service.registerFailure("ali@example.com")
        assert service.isBlocked("ali@example.com")

        when: "la duree du verrouillage s ecoule"
        Thread.sleep(1100)

        then: "le compte n est plus bloque"
        !service.isBlocked("ali@example.com")
    }

    void "la limite de creation de comptes est respectee par adresse"() {
        when: "les creations maximales sont atteintes"
        service.registerRegistration("register:127.0.0.1")
        service.registerRegistration("register:127.0.0.1")

        then: "la prochaine creation est refuse"
        service.registrationsInWindow("register:127.0.0.1") >= service.maxRegistrations
    }
}