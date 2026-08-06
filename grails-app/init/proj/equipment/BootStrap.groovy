package proj.equipment

import java.text.SimpleDateFormat

class BootStrap {

    def init = { servletContext ->
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

            if (TypeEquipement.count() > 0) return
            def sdf = new SimpleDateFormat("yyyy-MM-dd")

            def ordinateur = new TypeEquipement(nom: "Ordinateur").save(failOnError: true)
            def projecteur = new TypeEquipement(nom: "Projecteur").save(failOnError: true)
            def imprimante = new TypeEquipement(nom: "Imprimante").save(failOnError: true)
            def tablette = new TypeEquipement(nom: "Tablette").save(failOnError: true)
            def telephone = new TypeEquipement(nom: "Telephone").save(failOnError: true)

            def admin1 = new Personnel(nom: "Diop", prenom: "Mamadou", email: "mamadou.diop@example.com", motDePasse: "admin123", role: RolePersonnel.ADMIN).save(failOnError: true)
            new Personnel(nom: "Ndiaye", prenom: "Fatou", email: "fatou.ndiaye@example.com", motDePasse: "admin123", role: RolePersonnel.ADMIN).save(failOnError: true)
            def alice = new Personnel(nom: "Diallo", prenom: "Aissatou", email: "aissatou.diallo@example.com", motDePasse: "pass123", role: RolePersonnel.USER).save(failOnError: true)
            def bob = new Personnel(nom: "Fall", prenom: "Abdoulaye", email: "abdoulaye.fall@example.com", motDePasse: "pass123", role: RolePersonnel.USER).save(failOnError: true)
            def carole = new Personnel(nom: "Sarr", prenom: "Ndeye", email: "ndeye.sarr@example.com", motDePasse: "pass123", role: RolePersonnel.USER).save(failOnError: true)
            new Personnel(nom: "Mbaye", prenom: "Ousmane", email: "ousmane.mbaye@example.com", motDePasse: "pass123", role: RolePersonnel.USER).save(failOnError: true)
            new Personnel(nom: "Ba", prenom: "Marieme", email: "marieme.ba@example.com", motDePasse: "pass123", role: RolePersonnel.USER).save(failOnError: true)

            def eq2 = new Equipement(type: ordinateur, numeroSerie: "SN-001", description: "Station fixe HP EliteDesk 800", etat: EtatEquipement.AFFECTE).save(failOnError: true)
            new Equipement(type: ordinateur, numeroSerie: "SN-002", description: "PC portable Dell Latitude 5420", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            new Equipement(type: projecteur, numeroSerie: "SN-003", description: "Projecteur Epson EB-2155W", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq4 = new Equipement(type: projecteur, numeroSerie: "SN-004", description: "Projecteur BenQ MH535", etat: EtatEquipement.EN_PANNE).save(failOnError: true)
            new Equipement(type: imprimante, numeroSerie: "SN-005", description: "Imprimante laser Brother HL-L2370DW", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            new Equipement(type: tablette, numeroSerie: "SN-006", description: "iPad Air 11 (M2)", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq7 = new Equipement(type: tablette, numeroSerie: "SN-007", description: "Samsung Galaxy Tab S9", etat: EtatEquipement.AFFECTE).save(failOnError: true)
            new Equipement(type: telephone, numeroSerie: "SN-008", description: "iPhone 15 Pro", etat: EtatEquipement.DISPONIBLE).save(failOnError: true)
            def eq9 = new Equipement(type: telephone, numeroSerie: "SN-009", description: "Samsung Galaxy S24", etat: EtatEquipement.REPARE).save(failOnError: true)

            new Affectation(equipement: eq2, personnel: alice, dateAffectation: sdf.parse("2026-07-15")).save(failOnError: true)
            new Affectation(equipement: eq7, personnel: bob, dateAffectation: sdf.parse("2026-07-18")).save(failOnError: true)

            new Signalement(equipement: eq4, personnel: alice, type: TypeSignalement.PANNE, description: "L'image est floue et la mise au point ne fonctionne plus").save(failOnError: true)
            new Signalement(equipement: eq9, personnel: carole, type: TypeSignalement.PROBLEME_FONCTIONNEL, description: "La batterie se decharge tres vite").save(failOnError: true)
        }
    }

    def destroy = {}
}
