package proj.equipment

import grails.util.Environment
import org.springframework.beans.factory.annotation.Value

class BootStrap {

    @Value('${demo.adminPassword:}')
    String demoAdminPassword
    @Value('${demo.userPassword:}')
    String demoUserPassword

    def affectationService

    def init = { servletContext ->
        migrerTypeOrphelin()

        if (Environment.current != Environment.DEVELOPMENT) {
            log.info("Jeu de donnees de demonstration reserve a l'environnement de developpement")
            return
        }
        if (TypeEquipement.count() > 0) {
            log.info("Base deja initialisee, jeu de donnees de demonstration ignore")
            return
        }
        if (!demoAdminPassword || !demoUserPassword) {
            log.warn("Identifiants de demonstration absents (demo.adminPassword / demo.userPassword), seed ignore")
            return
        }
        creerJeuDeDonnees()
    }

    private void migrerTypeOrphelin() {
        TypeEquipement.withTransaction { status ->
            def typeAutre = TypeEquipement.findByNomIlike("Autre")
            if (typeAutre) {
                def autreEquipements = Equipement.findAllByType(typeAutre)
                if (autreEquipements) {
                    def fallback = TypeEquipement.findByNomIlike("Ordinateur")
                    autreEquipements.each { it.type = fallback; it.save() }
                }
                typeAutre.delete(flush: true)
            }
        }
    }

    private void creerJeuDeDonnees() {
        TypeEquipement.withTransaction { status ->
            def ordinateur = new TypeEquipement(nom: "Ordinateur").save(failOnError: true)
            def projecteur = new TypeEquipement(nom: "Projecteur").save(failOnError: true)
            def imprimante = new TypeEquipement(nom: "Imprimante").save(failOnError: true)
            def tablette = new TypeEquipement(nom: "Tablette").save(failOnError: true)
            def telephone = new TypeEquipement(nom: "Telephone").save(failOnError: true)

            def admin1 = new Personnel(nom: "Diop", prenom: "Mamadou", email: "mamadou.diop@example.com", motDePasse: demoAdminPassword, role: RolePersonnel.ADMIN).save(failOnError: true)
            new Personnel(nom: "Ndiaye", prenom: "Fatou", email: "fatou.ndiaye@example.com", motDePasse: demoAdminPassword, role: RolePersonnel.ADMIN).save(failOnError: true)
            def alice = new Personnel(nom: "Diallo", prenom: "Aissatou", email: "aissatou.diallo@example.com", motDePasse: demoUserPassword, role: RolePersonnel.USER).save(failOnError: true)
            def bob = new Personnel(nom: "Fall", prenom: "Abdoulaye", email: "abdoulaye.fall@example.com", motDePasse: demoUserPassword, role: RolePersonnel.USER).save(failOnError: true)
            def carole = new Personnel(nom: "Sarr", prenom: "Ndeye", email: "ndeye.sarr@example.com", motDePasse: demoUserPassword, role: RolePersonnel.USER).save(failOnError: true)
            new Personnel(nom: "Mbaye", prenom: "Ousmane", email: "ousmane.mbaye@example.com", motDePasse: demoUserPassword, role: RolePersonnel.USER).save(failOnError: true)
            new Personnel(nom: "Ba", prenom: "Marieme", email: "marieme.ba@example.com", motDePasse: demoUserPassword, role: RolePersonnel.USER).save(failOnError: true)

            def eq2 = new Equipement(type: ordinateur, numeroSerie: "SN-001", description: "Station fixe HP EliteDesk 800").save(failOnError: true)
            new Equipement(type: ordinateur, numeroSerie: "SN-002", description: "PC portable Dell Latitude 5420", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            new Equipement(type: projecteur, numeroSerie: "SN-003", description: "Projecteur Epson EB-2155W", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq4 = new Equipement(type: projecteur, numeroSerie: "SN-004", description: "Projecteur BenQ MH535").save(failOnError: true)
            eq4.etat = EtatEquipement.EN_PANNE
            eq4.save(failOnError: true)
            new Equipement(type: imprimante, numeroSerie: "SN-005", description: "Imprimante laser Brother HL-L2370DW", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            new Equipement(type: tablette, numeroSerie: "SN-006", description: "iPad Air 11 (M2)", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq7 = new Equipement(type: tablette, numeroSerie: "SN-007", description: "Samsung Galaxy Tab S9").save(failOnError: true)
            new Equipement(type: telephone, numeroSerie: "SN-008", description: "iPhone 15 Pro", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq9 = new Equipement(type: telephone, numeroSerie: "SN-009", description: "Samsung Galaxy S24").save(failOnError: true)
            eq9.etat = EtatEquipement.REPARE
            eq9.save(failOnError: true)

            def attribution1 = affectationService.attribuer(eq2, alice, admin1)
            if (!attribution1.success) throw new RuntimeException("Seed : ${attribution1.message}")
            def attribution2 = affectationService.attribuer(eq7, bob, admin1)
            if (!attribution2.success) throw new RuntimeException("Seed : ${attribution2.message}")

            new Signalement(equipement: eq4, personnel: alice, type: TypeSignalement.PANNE, description: "L'image est floue et la mise au point ne fonctionne plus").save(failOnError: true)
            new Signalement(equipement: eq9, personnel: carole, type: TypeSignalement.PROBLEME_FONCTIONNEL, description: "La batterie se decharge tres vite").save(failOnError: true)
        }
    }

    def destroy = {}
}
