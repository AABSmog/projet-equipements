package proj.equipment.service

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.event.ApplicationStartupEvent
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*

/**
 * Migration des types orphelins et jeu de donnees de demonstration
 * (developpement uniquement, si la base est vide).
 */
@Slf4j
@Singleton
class BootstrapSeed implements ApplicationEventListener<ApplicationStartupEvent> {

    @PersistenceContext EntityManager em
    @Inject AffectationService affectationService
    @Inject ValidationMessagesService validationMessagesService

    @Value('${app.demo.adminPassword:}')
    String demoAdminPassword

    @Value('${app.demo.userPassword:}')
    String demoUserPassword

    @Value('${app.demo.seed:true}')
    boolean seedEnabled

    @Inject RegleGestionService regleGestionService

    @Override
    @Transactional
    void onApplicationEvent(ApplicationStartupEvent event) {
        migrerTypeOrphelin()
        Etablissement principal = ensureDefaultEtablissement()
        // Migration des donnees existantes sans etablissement (single-DB mode)
        migrerDonneesSansEtablissement(principal)
        if (!seedEnabled) {
            log.info('Jeu de donnees de demonstration desactive (app.demo.seed=false)')
            return
        }
        long nbTypes = em.createQuery('select count(t) from TypeEquipement t', Long).singleResult
        if (nbTypes > 0) {
            log.info('Base deja initialisee, jeu de donnees de demonstration ignore')
            return
        }
        if (!demoAdminPassword || !demoUserPassword) {
            log.warn('Identifiants de demonstration absents (app.demo.adminPassword / app.demo.userPassword), seed ignore')
            return
        }
        creerJeuDeDonnees(principal)
    }

    private Etablissement ensureDefaultEtablissement() {
        List<Etablissement> r = em.createQuery('from Etablissement where slug = :s', Etablissement)
                .setParameter('s', 'principal').setMaxResults(1).resultList as List<Etablissement>
        if (r) return r[0]
        Etablissement e = new Etablissement(nom: 'Etablissement Principal', slug: 'principal', domaineEmail: 'example.com', statut: 'ACTIF')
        em.persist(e)
        em.flush()
        // regles par defaut
        RegleGestion.defaults(e.domaineEmail).each { k, v ->
            em.persist(new RegleGestion(etablissement: e, cle: k, valeur: v))
        }
        log.info('Etablissement principal cree (id={})', e.id)
        e
    }

    private void migrerDonneesSansEtablissement(Etablissement etab) {
        try {
            int p = em.createQuery('update Personnel p set p.etablissement = :etab where p.etablissement is null')
                    .setParameter('etab', etab).executeUpdate()
            int eq = em.createQuery('update Equipement e set e.etablissement = :etab where e.etablissement is null')
                    .setParameter('etab', etab).executeUpdate()
            if (p + eq > 0) log.info('Migration etablissement principal : {} personnels, {} equipements rattaches', p, eq)
        } catch (Exception ex) {
            log.warn('Migration etablissement ignoree: {}', ex.message)
        }
    }

    private void migrerTypeOrphelin() {
        TypeEquipement typeAutre = findByNomIlike('Autre')
        if (typeAutre) {
            List<Equipement> autreEquipements = em.createQuery('from Equipement where type.id = :tid', Equipement)
                    .setParameter('tid', typeAutre.id).resultList as List<Equipement>
            if (autreEquipements) {
                TypeEquipement fallback = findByNomIlike('Ordinateur')
                if (!fallback) {
                    fallback = new TypeEquipement(nom: 'Ordinateur')
                    em.persist(fallback)
                    em.flush()
                }
                autreEquipements.each { it.type = fallback; em.merge(it) }
            }
            em.remove(typeAutre)
            em.flush()
        }
    }

    private void creerJeuDeDonnees(Etablissement etab) {
        TypeEquipement ordinateur = save(new TypeEquipement(nom: 'Ordinateur'))
        TypeEquipement projecteur = save(new TypeEquipement(nom: 'Projecteur'))
        TypeEquipement imprimante = save(new TypeEquipement(nom: 'Imprimante'))
        TypeEquipement tablette = save(new TypeEquipement(nom: 'Tablette'))
        TypeEquipement telephone = save(new TypeEquipement(nom: 'Telephone'))

        Personnel admin1 = save(new Personnel(nom: 'Diop', prenom: 'Mamadou', email: 'mamadou.diopesp@example.com', motDePasse: demoAdminPassword, role: RolePersonnel.ADMIN, etablissement: etab))
        save(new Personnel(nom: 'Ndiaye', prenom: 'Fatou', email: 'fatou.ndiayeesp@example.com', motDePasse: demoAdminPassword, role: RolePersonnel.ADMIN, etablissement: etab))
        Personnel alice = save(new Personnel(nom: 'Diallo', prenom: 'Aissatou', email: 'aissatou.dialloesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        Personnel bob = save(new Personnel(nom: 'Fall', prenom: 'Abdoulaye', email: 'abdoulaye.fallesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        Personnel carole = save(new Personnel(nom: 'Sarr', prenom: 'Ndeye', email: 'ndeye.sarresp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Mbaye', prenom: 'Ousmane', email: 'ousmane.mbayeesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Ba', prenom: 'Marieme', email: 'marieme.baesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Traore', prenom: 'Issouf', email: 'issa.traoreesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Konate', prenom: 'Aminata', email: 'amina.konateesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Coulibaly', prenom: 'Yacouba', email: 'yacouba.coulibalyesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Sangare', prenom: 'Fatoumata', email: 'fatou.sangareesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Ouattara', prenom: 'Koffi', email: 'koffi.ouattaraesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Diao', prenom: 'Aida', email: 'aida.diaoesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Camara', prenom: 'Mohamed', email: 'mohamed.camaraesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
        save(new Personnel(nom: 'Sylla', prenom: 'Hadja', email: 'hadja.syllaesp@example.com', motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))

        Equipement eq2 = save(new Equipement(type: ordinateur, numeroSerie: 'SN-001', description: 'Station fixe HP EliteDesk 800', etablissement: etab))
        save(new Equipement(type: ordinateur, numeroSerie: 'SN-002', description: 'PC portable Dell Latitude 5420', etablissement: etab, etat: EtatEquipement.DISPONIBLE))
        save(new Equipement(type: projecteur, numeroSerie: 'SN-003', description: 'Projecteur Epson EB-2155W', etablissement: etab, etat: EtatEquipement.DISPONIBLE))
        Equipement eq4 = save(new Equipement(type: projecteur, numeroSerie: 'SN-004', description: 'Projecteur BenQ MH535', etablissement: etab, etat: EtatEquipement.EN_PANNE))
        save(new Equipement(type: imprimante, numeroSerie: 'SN-005', description: 'Imprimante laser Brother HL-L2370DW', etablissement: etab, etat: EtatEquipement.DISPONIBLE))
        save(new Equipement(type: tablette, numeroSerie: 'SN-006', description: 'iPad Air 11 (M2)', etat: EtatEquipement.DISPONIBLE, etablissement: etab))
        Equipement eq7 = save(new Equipement(type: tablette, numeroSerie: 'SN-007', description: 'Samsung Galaxy Tab S9', etablissement: etab))
        save(new Equipement(type: telephone, numeroSerie: 'SN-008', description: 'iPhone 15 Pro', etablissement: etab, etat: EtatEquipement.DISPONIBLE))
        Equipement eq9 = save(new Equipement(type: telephone, numeroSerie: 'SN-009', description: 'Samsung Galaxy S24', etablissement: etab, etat: EtatEquipement.REPARE))

        Map<String, Object> attribution1 = affectationService.attribuer(eq2, alice, admin1)
        if (!attribution1.success) throw new RuntimeException("Seed : ${attribution1.message}")
        Map<String, Object> attribution2 = affectationService.attribuer(eq7, bob, admin1)
        if (!attribution2.success) throw new RuntimeException("Seed : ${attribution2.message}")

        save(new Signalement(equipement: eq4, personnel: alice, type: TypeSignalement.PANNE, description: "L'image est floue et la mise au point ne fonctionne plus"))
        save(new Signalement(equipement: eq9, personnel: carole, type: TypeSignalement.PROBLEME_FONCTIONNEL, description: 'La batterie se decharge tres vite'))

        log.info('Jeu de donnees de demonstration cree')
    }

    private <T> T save(T entity) {
        em.persist(entity)
        entity
    }

    private TypeEquipement findByNomIlike(String nom) {
        List<TypeEquipement> r = em.createQuery('from TypeEquipement where lower(nom) = :n', TypeEquipement)
                .setParameter('n', nom.toLowerCase()).setMaxResults(1).resultList
        r ? r[0] : null
    }
}