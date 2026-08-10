package proj.equipment

import grails.testing.gorm.DataTest
import spock.lang.Specification

class EquipementSpec extends Specification implements DataTest {

    def setupSpec() {
        mockDomains Equipement, TypeEquipement, Affectation
    }

    private TypeEquipement type() {
        new TypeEquipement(nom: "Ordinateur").save(flush: true, failOnError: true)
    }

    void "un equipement valide est sauvegarde avec un numero de serie auto-genere"() {
        given: "un type existant"
        def t = type()

        when: "un equipement est cree sans numero de serie"
        def e = new Equipement(type: t, description: "Poste fixe").save(flush: true, failOnError: true)

        then: "il est sauvegarde a l'etat disponible et son SN est genere"
        e.id != null
        e.numeroSerie != null
        e.numeroSerie.startsWith("SN-")
        e.etat == EtatEquipement.DISPONIBLE
    }

    void "un equipement fourni avec un numero de serie le conserve"() {
        given: "un type existant"
        def t = type()

        when: "un equipement est cree avec un SN explicite"
        def e = new Equipement(type: t, description: "Poste", numeroSerie: "SN-ABC-001").save(flush: true, failOnError: true)

        then:
        e.numeroSerie == "SN-ABC-001"
    }

    void "la description est obligatoire"() {
        given:
        def t = type()

        when: "la description est vide"
        def e = new Equipement(type: t, description: " ")

        then:
        !e.validate()
        e.errors.getFieldError("description") != null
    }

    void "le type est obligatoire"() {
        when: "aucun type n'est renseigne"
        def e = new Equipement(description: "Sans type")

        then:
        !e.validate()
        e.errors.getFieldError("type") != null
    }

    void "le numero de serie est unique"() {
        given:
        def t = type()
        new Equipement(type: t, description: "Premier", numeroSerie: "SN-AAA").save(flush: true, failOnError: true)

        when: "un second equipement reprend le meme SN"
        def e = new Equipement(type: t, description: "Second", numeroSerie: "SN-AAA")

        then:
        !e.validate()
        e.errors.getFieldError("numeroSerie") != null
    }

    void "un nouvel equipement ne peut pas etre cree hors service ou affecte"() {
        given:
        def t = type()

        expect: "seul l'etat disponible est accepte a la creation"
        new Equipement(type: t, description: "x", etat: EtatEquipement.AFFECTE).validate() == false
        new Equipement(type: t, description: "x", etat: EtatEquipement.HORS_SERVICE).validate() == false
        new Equipement(type: t, description: "x", etat: EtatEquipement.DISPONIBLE).validate() == true
    }

    void "un equipement persiste ne peut pas passer a AFFECTE sans affectation active"() {
        given: "un equipement disponible deja persiste"
        def t = type()
        def e = new Equipement(type: t, description: "Poste").save(flush: true, failOnError: true)

        when: "on tente de le marquer affecte sans affectation"
        e.etat = EtatEquipement.AFFECTE

        then:
        !e.validate()
        e.errors.getFieldError("etat") != null
    }

    void "un equipement affecte ne peut pas redevenir disponible tant que l affectation est active"() {
        given: "un equipement avec une affectation active"
        def t = type()
        def pers = new Personnel(nom: "Diop", prenom: "Ali", email: "ali@example.com", motDePasse: "Secret123").save(flush: true, failOnError: true)
        def e = new Equipement(type: t, description: "Poste").save(flush: true, failOnError: true)
        new Affectation(equipement: e, personnel: pers, dateAffectation: new Date()).save(flush: true, failOnError: true)

        when: "on tente de le rendre disponible"
        e.etat = EtatEquipement.DISPONIBLE

        then:
        !e.validate()
        e.errors.getFieldError("etat") != null
    }
}
