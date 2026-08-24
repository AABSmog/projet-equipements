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
class ParcoursCompletIntegrationSpec extends Specification {

    @Inject AffectationService affectationService
    @Inject EntityManager em

    @Transactional
    void "parcours complet : creation, attribution, signalement, restitution et declassement"() {
        given: "un type, un administrateur, un employe et un equipement"
        def type = new TypeEquipement(nom: "Imprimante Integ-${UUID.randomUUID()}")
        em.persist(type); em.flush()
        def admin = new Personnel(nom: "Admin", prenom: "Mamadou", email: "admin.integ-${UUID.randomUUID()}@example.com", motDePasse: "Admin123", role: RolePersonnel.ADMIN)
        em.persist(admin); em.flush()
        def employe = new Personnel(nom: "Employe", prenom: "Aissatou", email: "employe.integ-${UUID.randomUUID()}@example.com", motDePasse: "User1234")
        em.persist(employe); em.flush()
        def equipement = new Equipement(type: type, description: "Imprimante laser")
        em.persist(equipement); em.flush()

        when: "l administrateur attribue l equipement a l employe"
        def attribution = affectationService.attribuer(equipement, employe, admin)

        then: "l affectation est creee et l equipement passe a AFFECTE"
        attribution.success
        equipement.etat == EtatEquipement.AFFECTE
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", equipement.id).singleResult as Affectation
        aff.personnel.id == employe.id
        aff.attribuePar.id == admin.id
        aff.dateRetour == null

        when: "une seconde attribution de l equipement est tentee"
        def autre = new Personnel(nom: "Autre", prenom: "Pers", email: "autre.integ-${UUID.randomUUID()}@example.com", motDePasse: "User1234")
        em.persist(autre); em.flush()
        def doubleAttribution = affectationService.attribuer(equipement, autre, admin)

        then: "elle est refusee et aucune affectation supplementaire n existe"
        !doubleAttribution.success
        em.createQuery("select count(a) from Affectation a where a.equipement.id = :id", Long).setParameter("id", equipement.id).singleResult == 1L

        when: "l employe signale un probleme sur l equipement"
        def signalement = new Signalement(equipement: equipement, personnel: employe, type: TypeSignalement.PANNE, description: "Toner bloque")
        em.persist(signalement); em.flush()

        then: "le signalement est enregistre"
        signalement.id != null

        when: "l employe restitue l equipement"
        def restitution = affectationService.restituer(equipement, employe, "Fin de mission")

        then: "le retour est enregistre et l equipement redevient disponible"
        restitution.success
        equipement.etat == EtatEquipement.DISPONIBLE
        def affRetournee = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", equipement.id).singleResult as Affectation
        affRetournee.dateRetour != null
        affRetournee.raisonRetour == "Fin de mission"

        when: "une restitution est tentee a nouveau"
        def restitutionBis = affectationService.restituer(equipement, employe, "Encore")

        then: "elle est refusee (plus d affectation active)"
        !restitutionBis.success

        when: "l administrateur declare l equipement hors service"
        def declassement = affectationService.declasser(equipement)

        then: "l equipement passe hors service"
        declassement.success
        equipement.etat == EtatEquipement.HORS_SERVICE
    }

    @Transactional
    void "l historique garde trace de l auteur de chaque attribution"() {
        given: "un type, deux personnels et un equipement"
        def type = new TypeEquipement(nom: "Projecteur Integ-${UUID.randomUUID()}")
        em.persist(type); em.flush()
        def admin = new Personnel(nom: "Admin", prenom: "Chef", email: "admin.hist-${UUID.randomUUID()}@example.com", motDePasse: "Admin123", role: RolePersonnel.ADMIN)
        em.persist(admin); em.flush()
        def employe = new Personnel(nom: "Employe", prenom: "User", email: "employe.hist-${UUID.randomUUID()}@example.com", motDePasse: "User1234")
        em.persist(employe); em.flush()
        def equipement = new Equipement(type: type, description: "Video projecteur")
        em.persist(equipement); em.flush()

        when: "une attribution puis une restitution sont effectuees"
        affectationService.attribuer(equipement, employe, admin)
        def aff = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", equipement.id).singleResult as Affectation
        affectationService.restituer(aff, "Fin de mission")

        then: "l affectation conserve l auteur et les dates"
        aff.attribuePar.id == admin.id
        aff.attribuePar.nom == "Admin"
        aff.dateAffectation != null
        aff.dateRetour != null
        def historique = em.createQuery("from Affectation where equipement.id = :id", Affectation).setParameter("id", equipement.id).resultList as List<Affectation>
        historique.size() == 1
        historique[0].attribuePar.email == admin.email
    }
}
