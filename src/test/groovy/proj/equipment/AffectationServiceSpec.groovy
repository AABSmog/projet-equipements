package proj.equipment

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class AffectationServiceSpec extends Specification implements ServiceUnitTest<AffectationService>, DataTest {

    def setupSpec() {
        mockDomains Affectation, Equipement, Personnel, TypeEquipement
    }

    private Personnel personnel(String nom = "Diop", String prenom = "Ali", String email = "ali@example.com") {
        new Personnel(nom: nom, prenom: prenom, email: email, motDePasse: "secret6").save(flush: true, failOnError: true)
    }

    private Equipement equipementDisponible() {
        def t = new TypeEquipement(nom: "Ordinateur").save(flush: true, failOnError: true)
        new Equipement(type: t, description: "Poste de travail").save(flush: true, failOnError: true)
    }

    void "attribuer un equipement disponible cree l affectation et passe a AFFECTE"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali", "ali@example.com")
        def admin = personnel("Admin", "Chef", "admin@example.com")

        when: "l admin attribue l equipement a l employe"
        def result = service.attribuer(eq, employe, admin)

        then: "l affectation est creee avec l auteur et l etat passe a AFFECTE"
        result.success == true
        Affectation.count() == 1
        eq.etat == EtatEquipement.AFFECTE
        def aff = Affectation.findByEquipement(eq)
        aff.personnel == employe
        aff.attribuePar == admin
        aff.dateAffectation != null
        aff.dateRetour == null
    }

    void "attribuer un equipement deja affecte est refuse"() {
        given:
        def eq = equipementDisponible()
        def admin = personnel("Admin", "Chef", "admin@example.com")
        service.attribuer(eq, personnel("Diop", "Ali", "ali@example.com"), admin)

        when: "un second employe tente de recuperer le meme equipement"
        def result = service.attribuer(eq, personnel("Autre", "Pers", "autre@example.com"), admin)

        then:
        result.success == false
        Affectation.count() == 1
        eq.etat == EtatEquipement.AFFECTE
    }

    void "attribuer le meme equipement deux fois au meme personnel est refuse"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali", "ali@example.com")
        def admin = personnel("Admin", "Chef", "admin@example.com")
        service.attribuer(eq, employe, admin)

        when:
        def result = service.attribuer(eq, employe, admin)

        then:
        result.success == false
        Affectation.count() == 1
    }

    void "attribuer un equipement indisponible est refuse"() {
        given: "un equipement en panne"
        def eq = equipementDisponible()
        eq.etat = EtatEquipement.EN_PANNE
        eq.save(flush: true, failOnError: true)

        when:
        def result = service.attribuer(eq, personnel(), personnel("A", "C", "admin@example.com"))

        then:
        result.success == false
        Affectation.count() == 0
    }

    void "attribuer avec un equipement ou un personnel inexistant est refuse"() {
        expect:
        service.attribuer(null, personnel(), null).success == false
        service.attribuer(equipementDisponible(), null, null).success == false
    }

    void "restituer une affectation en cours enregistre le retour et repasse a DISPONIBLE"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali", "ali@example.com")
        def admin = personnel("Admin", "Chef", "admin@example.com")
        service.attribuer(eq, employe, admin)
        def aff = Affectation.findByEquipement(eq)

        when: "le retour est enregistre avec une raison"
        def result = service.restituer(aff, "Fin de mission")

        then:
        result.success == true
        aff.dateRetour != null
        aff.raisonRetour == "Fin de mission"
        eq.etat == EtatEquipement.DISPONIBLE
    }

    void "restituer une affectation deja cloturee est refuse"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C", "admin@example.com"))
        def aff = Affectation.findByEquipement(eq)
        service.restituer(aff, "Retour initial")

        when:
        def result = service.restituer(aff, "Second retour")

        then:
        result.success == false
        aff.raisonRetour == "Retour initial"
    }

    void "restituer sans affectation active pour cet equipement et ce personnel est refuse"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.restituer(eq, personnel(), "Raison")

        then:
        result.success == false
        Affectation.count() == 0
    }

    void "desaffecter un equipement affecte libere l equipement"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C", "admin@example.com"))

        when:
        def result = service.desaffecter(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.DISPONIBLE
        Affectation.findByEquipementAndDateRetourIsNull(eq) == null
        Affectation.findByEquipement(eq).dateRetour != null
    }

    void "desaffecter un equipement non affecte est refuse"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.desaffecter(eq)

        then:
        result.success == false
        eq.etat == EtatEquipement.DISPONIBLE
    }

    void "declasser un equipement affecte cloture l affectation et passe a HORS_SERVICE"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C", "admin@example.com"))

        when:
        def result = service.declasser(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.HORS_SERVICE
        def aff = Affectation.findByEquipement(eq)
        aff.dateRetour != null
        aff.infoEquipement?.contains("declasse")
    }

    void "declasser un equipement disponible passe a HORS_SERVICE"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.declasser(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.HORS_SERVICE
        Affectation.count() == 0
    }
}
