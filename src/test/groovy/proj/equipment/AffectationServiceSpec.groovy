package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*
import proj.equipment.service.AffectationService
import spock.lang.Specification
import jakarta.transaction.Transactional

@MicronautTest
class AffectationServiceSpec extends Specification {

    @Inject AffectationService service
    @Inject EntityManager em

    private Personnel personnel(String nom = "Diop", String prenom = "Ali", String email = null) {
        email = email ?: "ali-${UUID.randomUUID()}@example.com"
        def p = new Personnel(nom: nom, prenom: prenom, email: email, motDePasse: "Secret123")
        em.persist(p)
        em.flush()
        p
    }

    private Equipement equipementDisponible() {
        def t = new TypeEquipement(nom: "Ordinateur-${UUID.randomUUID()}")
        em.persist(t)
        em.flush()
        def eq = new Equipement(type: t, description: "Poste de travail")
        em.persist(eq)
        em.flush()
        eq
    }

    @Transactional
    void "attribuer un equipement disponible cree l affectation et passe a AFFECTE"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali")
        def admin = personnel("Admin", "Chef")

        when:
        def result = service.attribuer(eq, employe, admin)

        then:
        result.success == true
        eq.etat == EtatEquipement.AFFECTE
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", eq.id).singleResult as Affectation
        aff.personnel.id == employe.id
        aff.attribuePar.id == admin.id
        aff.dateAffectation != null
        aff.dateRetour == null
    }

    @Transactional
    void "attribuer un equipement deja affecte est refuse"() {
        given:
        def eq = equipementDisponible()
        def admin = personnel("Admin", "Chef")
        service.attribuer(eq, personnel("Diop", "Ali"), admin)

        when:
        def result = service.attribuer(eq, personnel("Autre", "Pers"), admin)

        then:
        result.success == false
        em.createQuery("select count(a) from Affectation a where a.equipement.id = :id", Long).setParameter("id", eq.id).singleResult == 1L
        eq.etat == EtatEquipement.AFFECTE
    }

    @Transactional
    void "attribuer le meme equipement deux fois au meme personnel est refuse"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali")
        def admin = personnel("Admin", "Chef")
        service.attribuer(eq, employe, admin)

        when:
        def result = service.attribuer(eq, employe, admin)

        then:
        result.success == false
    }

    @Transactional
    void "attribuer un equipement indisponible est refuse"() {
        given: "un equipement en panne"
        def eq = equipementDisponible()
        eq.etat = EtatEquipement.EN_PANNE
        em.merge(eq)
        em.flush()

        when:
        def result = service.attribuer(eq, personnel(), personnel("A", "C"))

        then:
        result.success == false
        em.createQuery("select count(a) from Affectation a where a.equipement.id = :id", Long).setParameter("id", eq.id).singleResult == 0L
    }

    void "attribuer avec un equipement ou un personnel inexistant est refuse"() {
        expect:
        service.attribuer(null, personnel(), null).success == false
    }

    @Transactional
    void "restituer une affectation en cours enregistre le retour et repasse a DISPONIBLE"() {
        given:
        def eq = equipementDisponible()
        def employe = personnel("Diop", "Ali")
        def admin = personnel("Admin", "Chef")
        service.attribuer(eq, employe, admin)
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", eq.id).singleResult as Affectation

        when:
        def result = service.restituer(aff, "Fin de mission")

        then:
        result.success == true
        aff.dateRetour != null
        aff.raisonRetour == "Fin de mission"
        eq.etat == EtatEquipement.DISPONIBLE
    }

    @Transactional
    void "restituer une affectation deja cloturee est refuse"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C"))
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", eq.id).singleResult as Affectation
        service.restituer(aff, "Retour initial")

        when:
        def result = service.restituer(aff, "Second retour")

        then:
        result.success == false
        aff.raisonRetour == "Retour initial"
    }

    @Transactional
    void "restituer sans affectation active pour cet equipement et ce personnel est refuse"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.restituer(eq, personnel(), "Raison")

        then:
        result.success == false
    }

    @Transactional
    void "desaffecter un equipement affecte libere l equipement"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C"))

        when:
        def result = service.desaffecter(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.DISPONIBLE
        em.createQuery("from Affectation where equipement.id = :id and dateRetour is null", Affectation).setParameter("id", eq.id).resultList.isEmpty()
    }

    @Transactional
    void "desaffecter un equipement non affecte est refuse"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.desaffecter(eq)

        then:
        result.success == false
        eq.etat == EtatEquipement.DISPONIBLE
    }

    @Transactional
    void "declasser un equipement affecte cloture l affectation et passe a HORS_SERVICE"() {
        given:
        def eq = equipementDisponible()
        service.attribuer(eq, personnel(), personnel("A", "C"))

        when:
        def result = service.declasser(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.HORS_SERVICE
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", eq.id).singleResult as Affectation
        aff.dateRetour != null
        aff.infoEquipement?.contains("declasse")
    }

    @Transactional
    void "declasser un equipement disponible passe a HORS_SERVICE"() {
        given:
        def eq = equipementDisponible()

        when:
        def result = service.declasser(eq)

        then:
        result.success == true
        eq.etat == EtatEquipement.HORS_SERVICE
    }
}
