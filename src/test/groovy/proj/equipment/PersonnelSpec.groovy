package proj.equipment

import grails.testing.gorm.DataTest
import spock.lang.Specification

class PersonnelSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains Personnel
    }

    void "un personnel valide est sauvegarde et son mot de passe est hache"() {
        when: "un personnel valide est cree"
        def p = new Personnel(nom: "Diop", prenom: "Ali", email: "ali@example.com", motDePasse: "Secret123").save(flush: true, failOnError: true)

        then: "le mot de passe n'est pas stocke en clair"
        p.id != null
        !p.motDePasse.startsWith("Secret123")
        p.motDePasse.startsWith("\$2")
    }

    void "le nom et le prenom sont obligatoires"() {
        expect:
        new Personnel(nom: "", prenom: "Ali", email: "a@b.c", motDePasse: "Secret123").validate() == false
        new Personnel(nom: "Diop", prenom: "", email: "a@b.c", motDePasse: "Secret123").validate() == false
    }

    void "l email est obligatoire et doit etre valide"() {
        expect:
        new Personnel(nom: "D", prenom: "P", email: "", motDePasse: "Secret123").validate() == false
        new Personnel(nom: "D", prenom: "P", email: "email-invalide", motDePasse: "Secret123").validate() == false
    }

    void "le mot de passe doit contenir au moins 8 caracteres"() {
        when: "un mot de passe trop court est fourni"
        def p = new Personnel(nom: "D", prenom: "P", email: "court@example.com", motDePasse: "abc")

        then:
        !p.validate()
        p.errors.getFieldError("motDePasse") != null
    }

    void "le mot de passe doit melanger minuscule, majuscule et chiffre"() {
        expect: "un mot de passe sans majuscule, sans chiffre ou trop simple est refusé"
        new Personnel(nom: "D", prenom: "P", email: "pol1@example.com", motDePasse: "abcdefgh").validate() == false
        new Personnel(nom: "D", prenom: "P", email: "pol2@example.com", motDePasse: "ABCDEFGH").validate() == false
        new Personnel(nom: "D", prenom: "P", email: "pol3@example.com", motDePasse: "Abcdefgh").validate() == false

        and: "un mot de passe conforme (8+ caracteres, minuscule, majuscule, chiffre) est accepte"
        new Personnel(nom: "D", prenom: "P", email: "pol4@example.com", motDePasse: "Password123").validate() == true
    }

    void "l email est unique"() {
        given: "un premier personnel avec un email donne"
        new Personnel(nom: "A", prenom: "B", email: "dup@example.com", motDePasse: "Secret123").save(flush: true, failOnError: true)

        when: "un second personnel reprend le meme email"
        def p = new Personnel(nom: "C", prenom: "D", email: "dup@example.com", motDePasse: "Secret123")

        then:
        !p.validate()
        p.errors.getFieldError("email") != null
    }

    void "le role par defaut d un nouveau personnel est USER"() {
        when:
        def p = new Personnel(nom: "D", prenom: "P", email: "role@example.com", motDePasse: "Secret123").save(flush: true, failOnError: true)

        then:
        p.role == RolePersonnel.USER
    }
}
