package proj.equipment

import grails.testing.gorm.DataTest
import spock.lang.Specification

class PersonnelSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains Personnel
    }

    void "un personnel valide est sauvegarde et son mot de passe est hache"() {
        when: "un personnel valide est cree"
        def p = new Personnel(nom: "Diop", prenom: "Ali", email: "ali@example.com", motDePasse: "secret6").save(flush: true, failOnError: true)

        then: "le mot de passe n'est pas stocke en clair"
        p.id != null
        !p.motDePasse.startsWith("secret6")
        p.motDePasse.startsWith("\$2")
    }

    void "le nom et le prenom sont obligatoires"() {
        expect:
        new Personnel(nom: "", prenom: "Ali", email: "a@b.c", motDePasse: "secret6").validate() == false
        new Personnel(nom: "Diop", prenom: "", email: "a@b.c", motDePasse: "secret6").validate() == false
    }

    void "l email est obligatoire et doit etre valide"() {
        expect:
        new Personnel(nom: "D", prenom: "P", email: "", motDePasse: "secret6").validate() == false
        new Personnel(nom: "D", prenom: "P", email: "email-invalide", motDePasse: "secret6").validate() == false
    }

    void "le mot de passe doit contenir au moins 6 caracteres"() {
        when: "un mot de passe trop court est fourni"
        def p = new Personnel(nom: "D", prenom: "P", email: "court@example.com", motDePasse: "abc")

        then:
        !p.validate()
        p.errors.getFieldError("motDePasse") != null
    }

    void "l email est unique"() {
        given: "un premier personnel avec un email donne"
        new Personnel(nom: "A", prenom: "B", email: "dup@example.com", motDePasse: "secret6").save(flush: true, failOnError: true)

        when: "un second personnel reprend le meme email"
        def p = new Personnel(nom: "C", prenom: "D", email: "dup@example.com", motDePasse: "secret6")

        then:
        !p.validate()
        p.errors.getFieldError("email") != null
    }

    void "le role par defaut d un nouveau personnel est USER"() {
        when:
        def p = new Personnel(nom: "D", prenom: "P", email: "role@example.com", motDePasse: "secret6").save(flush: true, failOnError: true)

        then:
        p.role == RolePersonnel.USER
    }
}
