package proj.equipment

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.TypeEquipement
import proj.equipment.service.ValidationMessagesService
import spock.lang.Specification
import jakarta.transaction.Transactional

@MicronautTest
class TypeEquipementSpec extends Specification {

    @Inject ValidationMessagesService validation
    @Inject EntityManager em

    void "le nom est obligatoire"() {
        expect:
        validation.validateTypeEquipement(new TypeEquipement(nom: ""))?.contains("obligatoire")
        validation.validateTypeEquipement(new TypeEquipement(nom: null))?.contains("obligatoire")
    }

    @Transactional
    void "le nom ne peut pas etre duplique exactement"() {
        given:
        def nom = "TypeDup-${UUID.randomUUID()}"
        em.persist(new TypeEquipement(nom: nom))
        em.flush()

        expect:
        validation.validateTypeEquipement(new TypeEquipement(nom: nom)).contains("existe deja")
    }

    @Transactional
    void "le nom ne peut pas etre duplique avec une casse differente"() {
        given:
        def nom = "Scanner-${UUID.randomUUID()}"
        em.persist(new TypeEquipement(nom: nom))
        em.flush()

        expect:
        validation.validateTypeEquipement(new TypeEquipement(nom: nom.toLowerCase())).contains("existe deja")
        validation.validateTypeEquipement(new TypeEquipement(nom: nom.toUpperCase())).contains("existe deja")
    }

    @Transactional
    void "un type valide est sauvegarde"() {
        when:
        def t = new TypeEquipement(nom: "TypeValide-${UUID.randomUUID()}")
        String err = validation.validateTypeEquipement(t)

        then:
        err == null
        when:
        em.persist(t)
        em.flush()
        then:
        t.id != null
    }
}
