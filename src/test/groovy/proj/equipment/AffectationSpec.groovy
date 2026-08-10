package proj.equipment

import grails.testing.gorm.DataTest
import spock.lang.Specification

class AffectationSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains Affectation, Equipement, TypeEquipement, Personnel
    }

    private Equipement equipement() {
        def t = new TypeEquipement(nom: "Ordinateur").save(flush: true, failOnError: true)
        new Equipement(type: t, description: "Poste de travail").save(flush: true, failOnError: true)
    }

    private Personnel personnel() {
        new Personnel(nom: "Diop", prenom: "Ali", email: "ali@example.com", motDePasse: "secret6").save(flush: true, failOnError: true)
    }

    void "une affectation valide est sauvegarde"() {
        given:
        def eq = equipement()
        def pers = personnel()

        when:
        def a = new Affectation(equipement: eq, personnel: pers, dateAffectation: new Date()).save(flush: true)

        then:
        a != null
        a.id != null
        a.dateRetour == null
    }

    void "le personnel et la date d affectation sont obligatoires"() {
        given:
        def eq = equipement()

        expect:
        new Affectation(equipement: eq, personnel: null, dateAffectation: new Date()).validate() == false
        new Affectation(equipement: eq, personnel: personnel(), dateAffectation: null).validate() == false
    }

    void "la date de retour ne peut pas etre anterieure a la date d affectation"() {
        given: "une affectation du 1er janvier 2026"
        def eq = equipement()
        def aff = new Affectation(
            equipement: eq,
            personnel: personnel(),
            dateAffectation: Date.parse("yyyy-MM-dd", "2026-01-01")
        )

        when: "une date de retour anterieure est renseignee"
        aff.dateRetour = Date.parse("yyyy-MM-dd", "2025-12-31")

        then:
        !aff.validate()
        aff.errors.getFieldError("dateRetour") != null
    }
}
