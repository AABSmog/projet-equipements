package proj.equipment

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class ParcoursCompletIntegrationSpec extends Specification {

    AffectationService affectationService

    void "parcours complet : creation, attribution, signalement, restitution et declassement"() {
        given: "un type, un administrateur, un employe et un equipement"
        def type = new TypeEquipement(nom: "Imprimante Integ").save(flush: true, failOnError: true)
        def admin = new Personnel(
            nom: "Admin", prenom: "Mamadou", email: "admin.integ@example.com",
            motDePasse: "Admin123", role: RolePersonnel.ADMIN
        ).save(flush: true, failOnError: true)
        def employe = new Personnel(
            nom: "Employe", prenom: "Aissatou", email: "employe.integ@example.com",
            motDePasse: "User1234"
        ).save(flush: true, failOnError: true)
        def equipement = new Equipement(type: type, description: "Imprimante laser").save(flush: true, failOnError: true)

        when: "l administrateur attribue l equipement a l employe"
        def attribution = affectationService.attribuer(equipement, employe, admin)

        then: "l affectation est creee et l equipement passe a AFFECTE"
        attribution.success
        equipement.etat == EtatEquipement.AFFECTE
        def aff = Affectation.findByEquipement(equipement)
        aff.personnel == employe
        aff.attribuePar == admin
        aff.dateRetour == null

        when: "une seconde attribution de l equipement est tentee"
        def autre = new Personnel(
            nom: "Autre", prenom: "Pers", email: "autre.integ@example.com", motDePasse: "User1234"
        ).save(flush: true, failOnError: true)
        def doubleAttribution = affectationService.attribuer(equipement, autre, admin)

        then: "elle est refusee et aucune affectation supplementaire n existe"
        !doubleAttribution.success
        Affectation.countByEquipement(equipement) == 1

        when: "l employe signale un probleme sur l equipement"
        def signalement = new Signalement(
            equipement: equipement,
            personnel: employe,
            type: TypeSignalement.PANNE,
            description: "Toner bloque"
        ).save(flush: true, failOnError: true)

        then: "le signalement est enregistre"
        signalement.id != null
        Signalement.countByEquipement(equipement) == 1

        when: "l employe restitue l equipement"
        def restitution = affectationService.restituer(equipement, employe, "Fin de mission")

        then: "le retour est enregistre et l equipement redevient disponible"
        restitution.success
        equipement.etat == EtatEquipement.DISPONIBLE
        def affRetournee = Affectation.findByEquipement(equipement)
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
        Affectation.findByEquipement(equipement).dateRetour != null
    }

    void "l historique garde trace de l auteur de chaque attribution"() {
        given: "un type, deux personnels et un equipement"
        def type = new TypeEquipement(nom: "Projecteur Integ").save(flush: true, failOnError: true)
        def admin = new Personnel(
            nom: "Admin", prenom: "Chef", email: "admin.hist@example.com",
            motDePasse: "Admin123", role: RolePersonnel.ADMIN
        ).save(flush: true, failOnError: true)
        def employe = new Personnel(
            nom: "Employe", prenom: "User", email: "employe.hist@example.com", motDePasse: "User1234"
        ).save(flush: true, failOnError: true)
        def equipement = new Equipement(type: type, description: "Video projecteur").save(flush: true, failOnError: true)

        when: "une attribution puis une restitution sont effectuees"
        affectationService.attribuer(equipement, employe, admin)
        def aff = Affectation.findByEquipement(equipement)
        affectationService.restituer(aff, "Fin de mission")

        then: "l affectation conserve l auteur et les dates"
        aff.attribuePar == admin
        aff.attribuePar.nom == "Admin"
        aff.dateAffectation != null
        aff.dateRetour != null
        def historique = Affectation.findAllByEquipement(equipement)
        historique.size() == 1
        historique[0].attribuePar.email == "admin.hist@example.com"
    }
}
