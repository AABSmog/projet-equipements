package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*
import proj.equipment.service.ValidationMessagesService
import spock.lang.Specification
import jakarta.transaction.Transactional

@MicronautTest
class EquipementSpec extends Specification {

    @Inject ValidationMessagesService validation
    @Inject EntityManager em

    @Transactional
    void "un equipement valide est sauvegarde avec un numero de serie auto-genere"() {
        given:
        def type = persist(new TypeEquipement(nom: "Ordinateur-${UUID.randomUUID()}"))

        when:
        def eq = new Equipement(type: type, description: "Poste de travail")
        String err = validation.validateEquipement(eq, true)

        then:
        err == null
        when:
        em.persist(eq)
        em.flush()
        then:
        eq.id != null
        eq.numeroSerie.startsWith("SN-")
    }

    @Transactional
    void "un equipement fourni avec un numero de serie le conserve"() {
        given:
        def type = persist(new TypeEquipement(nom: "Proj-${UUID.randomUUID()}"))

        when:
        def eq = new Equipement(type: type, numeroSerie: "SN-TEST-${UUID.randomUUID()}", description: "Projecteur")
        em.persist(eq)
        em.flush()

        then:
        eq.numeroSerie.startsWith("SN-TEST-")
    }

    void "la description est obligatoire"() {
        given:
        def type = new TypeEquipement(nom: "Ordinateur")
        def eq = new Equipement(type: type, description: "")

        expect:
        validation.validateEquipement(eq, true).contains("description")
    }

    void "le type est obligatoire"() {
        given:
        def eq = new Equipement(type: null, description: "Sans type")

        expect:
        validation.validateEquipement(eq, true).contains("type")
    }

    @Transactional
    void "le numero de serie est unique"() {
        given:
        def type = persist(new TypeEquipement(nom: "Imprimante-${UUID.randomUUID()}"))
        def sn = "SN-DUP-${UUID.randomUUID()}"
        em.persist(new Equipement(type: type, numeroSerie: sn, description: "Imprimante 1"))
        em.flush()

        when:
        def dup = new Equipement(type: type, numeroSerie: sn, description: "Imprimante 2")
        String err = validation.validateEquipement(dup, true)

        then:
        err.contains("deja utilise")
    }

    void "un nouvel equipement ne peut pas etre cree hors service ou affecte"() {
        given:
        def type = new TypeEquipement(nom: "Ordinateur")
        def eq = new Equipement(type: type, description: "Test", etat: EtatEquipement.HORS_SERVICE)

        expect:
        validation.validateEquipement(eq, true).contains("disponible")
    }

    @Transactional
    void "un equipement persiste ne peut pas passer a AFFECTE sans affectation active"() {
        given:
        def type = persist(new TypeEquipement(nom: "Tab-${UUID.randomUUID()}"))
        def eq = new Equipement(type: type, description: "Tablette")
        em.persist(eq)
        em.flush()
        eq.etat = EtatEquipement.AFFECTE

        expect:
        validation.validateEquipement(eq, false).contains("affecte")
    }

    @Transactional
    void "un equipement affecte ne peut pas redevenir disponible tant que l affectation est active"() {
        given:
        def type = persist(new TypeEquipement(nom: "Tel-${UUID.randomUUID()}"))
        def pers = persist(new Personnel(nom: "Test", prenom: "User", email: "user-${UUID.randomUUID()}@example.com", motDePasse: "User1234"))
        def eq = new Equipement(type: type, description: "Phone", etat: EtatEquipement.DISPONIBLE)
        em.persist(eq)
        em.flush()
        def aff = new Affectation(equipement: eq, personnel: pers, dateAffectation: new Date())
        em.persist(aff)
        eq.etat = EtatEquipement.AFFECTE
        em.merge(eq)
        em.flush()

        when:
        eq.etat = EtatEquipement.DISPONIBLE
        String err = validation.validateEquipement(eq, false)

        then:
        err.contains("encore affecte")
    }

    private <T> T persist(T entity) {
        em.persist(entity)
        em.flush()
        entity
    }
}
