package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import proj.equipment.domain.RolePersonnel
import proj.equipment.service.ValidationMessagesService
import spock.lang.Specification
import jakarta.transaction.Transactional

@MicronautTest
class PersonnelSpec extends Specification {

    @Inject ValidationMessagesService validation
    @Inject EntityManager em

    @Transactional
    void "un personnel valide est sauvegarde et son mot de passe est hache"() {
        when:
        def p = new Personnel(nom: "Diop", prenom: "Mamadou", email: "test-${UUID.randomUUID()}@example.com", motDePasse: "Secret123")
        String err = validation.validatePersonnel(p, true)

        then:
        err == null
        when:
        em.persist(p)
        em.flush()
        then:
        p.id != null
        p.motDePasse.startsWith('$2')
    }

    void "le nom et le prenom sont obligatoires"() {
        expect:
        validation.validatePersonnel(new Personnel(nom: "", prenom: "Ali", email: "a@example.com", motDePasse: "Secret123"), true).contains("nom")
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "", email: "a@example.com", motDePasse: "Secret123"), true).contains("prenom")
    }

    void "l email est obligatoire et doit etre valide"() {
        expect:
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "", motDePasse: "Secret123"), true).contains("email")
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "invalide", motDePasse: "Secret123"), true).contains("email")
    }

    void "le mot de passe doit contenir au moins 8 caracteres"() {
        expect:
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "Ab1"), true).contains("8 caracteres")
    }

    void "le mot de passe doit melanger minuscule, majuscule et chiffre"() {
        expect:
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "abcdefgh"), true).contains("minuscule")
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "ABCDEFGH"), true).contains("minuscule")
        validation.validatePersonnel(new Personnel(nom: "Diop", prenom: "Ali", email: "a@example.com", motDePasse: "Abcdefgh"), true).contains("minuscule")
    }

    @Transactional
    void "l email est unique"() {
        given:
        def email = "dup-${UUID.randomUUID()}@example.com"
        em.persist(new Personnel(nom: "Diop", prenom: "Ali", email: email, motDePasse: "Secret123"))
        em.flush()

        when:
        def dup = new Personnel(nom: "Autre", prenom: "Pers", email: email, motDePasse: "Secret123")
        String err = validation.validatePersonnel(dup, true)

        then:
        err.contains("deja utilise")
    }

    void "le role par defaut d un nouveau personnel est USER"() {
        expect:
        new Personnel().role == RolePersonnel.USER
    }
}
