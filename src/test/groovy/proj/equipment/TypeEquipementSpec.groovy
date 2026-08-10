package proj.equipment

import grails.testing.gorm.DataTest
import spock.lang.Specification

class TypeEquipementSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains TypeEquipement
    }

    void "le nom est obligatoire"() {
        when: "un type sans nom est enregistre"
        def t = new TypeEquipement(nom: " ")

        then:
        !t.validate()
        t.errors.getFieldError("nom") != null
    }

    void "le nom ne peut pas etre duplique exactement"() {
        given:
        new TypeEquipement(nom: "Scanner").save(flush: true, failOnError: true)

        when:
        def t = new TypeEquipement(nom: "Scanner")

        then:
        !t.validate()
        t.errors.getFieldError("nom") != null
    }

    void "le nom ne peut pas etre duplique avec une casse differente"() {
        given:
        new TypeEquipement(nom: "Scanner").save(flush: true, failOnError: true)

        when: "le meme nom est saisi en minuscules"
        def t = new TypeEquipement(nom: "scanner")

        then:
        !t.validate()
        t.errors.getFieldError("nom") != null
    }

    void "un type valide est sauvegarde"() {
        when:
        def t = new TypeEquipement(nom: "Imprimante").save(flush: true)

        then:
        t != null
        t.id != null
    }
}
