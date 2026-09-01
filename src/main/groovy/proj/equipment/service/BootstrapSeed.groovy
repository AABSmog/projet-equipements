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
        uniformiserMotsDePasseEtNettoyer()
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
        creerEtablissementsDeDemonstration(principal)
    }

    private void uniformiserMotsDePasseEtNettoyer() {
        try {
            // Mots de passe realistes par utilisateur (cf. USERS_MOTS_DE_PASSE.txt)
            Map<String,String> real = [
                'mamadou.diop@example.com':'Mamadou2024!', 'fatou.ndiaye@example.com':'Fatou2025#',
                'aissatou.diallo@example.com':'Aissatou24$', 'abdoulaye.fall@example.com':'Abdoulaye2024!',
                'ndeye.sarr@example.com':'NdeyeSarr2025!', 'ousmane.mbaye@example.com':'Ousmane2024#',
                'marieme.ba@example.com':'MariemeBa2025$', 'coumba.bousso@example.com':'Coumba2024!',
                'adama.danfa@example.com':'Adama2025!', 'mamadou@gmail.com':'MamadouG2024#',
                'assane.bousso@example.com':'Assane2024!', 'admin@etabtest.com':'AdminEtab2025!',
                'emp@etabtest.com':'EmpTest2024#', 'mohamed.diop@iam.sn':'Mohamed2024!',
                'assane.bousso@iam.sn':'AssaneIam2025$', 'mamadou.diop@ism.edu.sn':'MamadouIsm2024!',
                'assane.bousso@ism.edu.sn':'AssaneIsm2025#', 'admin@testfinal.com':'AdminFinal2024!',
                'mamadou@test.com':'MamadouTest2025$', 'mamadou2@test2.com':'Mamadou2_2024!',
                'final.test@final.com':'FinalTest2024#', 'fix.jet@jetfix.com':'FixJet2025!',
                'test.final2@final2.com':'Final2_2024$', 'm.diop@somdop.com':'SomdopM2024!',
                'f.ndiaye@somdop.com':'SomdopF2025#', 'a.diallo@somdop.com':'SomdopA2024$',
                'a.fall@somdop.com':'SomdopFall2025!', 'y.coulibaly@coulibaly-industries.com':'Yacouba2024!',
                'a.konate@coulibaly-industries.com':'Aminata2025#', 'f.sangare@coulibaly-industries.com':'Fatoumata2024$',
                'k.ouattara@coulibaly-industries.com':'Koffi2025!'
            ]
            List<Personnel> all = em.createQuery('from Personnel', Personnel).resultList as List<Personnel>
            log.info('Uniformisation mdp realistes: {} comptes trouves', all.size())
            int updated = 0
            for (Personnel p : all) {
                String key = p.email?.toLowerCase()
                String nv = real.get(key)
                if (!nv) {
                    // fallback realiste base sur prenom/nom
                    String base = (p.prenom ?: 'User').replaceAll('[^A-Za-z]', '')
                    nv = base.capitalize() + '2024!'
                    if (nv.length() < 8) nv = 'Assane2024!'
                }
                p.motDePasse = nv
                em.merge(p)
                updated++
            }
            if (updated > 0) {
                em.flush()
                log.info('Mots de passe realistes appliques : {} comptes', updated)
            }
            // Nettoyage leger des etablissements de test parasites (slug test-*)
            List<Etablissement> toClean = em.createQuery('from Etablissement where slug like :p', Etablissement)
                    .setParameter('p', 'test%').resultList as List<Etablissement>
            // Garder les 2 principaux de demo, supprimer les autres test-* excedentaires (limite a 2 etablissements de test max)
            if (toClean.size() > 3) {
                // Ne supprimer que les plus recents au-dela de 3
                toClean.sort { a, b -> a.id <=> b.id }
                List<Etablissement> excess = toClean.drop(3)
                for (Etablissement e : excess) {
                    try {
                        // Suppression en cascade manuelle (affectations, signalements, equipements, personnels, regles)
                        em.createQuery('delete from Signalement where equipement.etablissement = :e or personnel.etablissement = :e').setParameter('e', e).executeUpdate()
                        em.createQuery('delete from Affectation where equipement.etablissement = :e').setParameter('e', e).executeUpdate()
                        em.createQuery('delete from Equipement where etablissement = :e').setParameter('e', e).executeUpdate()
                        em.createQuery('delete from Personnel where etablissement = :e').setParameter('e', e).executeUpdate()
                        em.createQuery('delete from RegleGestion where etablissement = :e').setParameter('e', e).executeUpdate()
                        em.remove(em.contains(e) ? e : em.merge(e))
                        log.info('Etablissement de test supprime: {} ({})', e.nom, e.slug)
                    } catch (Exception ex) {
                        log.warn('Nettoyage etablissement {} echoue: {}', e.slug, ex.message)
                    }
                }
                em.flush()
            }
        } catch (Exception ex) {
            log.warn('Uniformisation/nettoyage ignore: {}', ex.message)
        }
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

    private void creerEtablissementsDeDemonstration(Etablissement principal) {
        // Reduit a 2 etablissements de demo (demande: reduire les etablissements de test)
        List<Map<String, Object>> etablissements = [
            [nom: 'SOMDOP Consulting', slug: 'somdop', domaineEmail: 'somdop.com', admins: [
                [nom: 'Diop', prenom: 'Mamadou', email: 'm.diop@somdop.com', role: RolePersonnel.ADMIN],
                [nom: 'Ndiaye', prenom: 'Fatou', email: 'f.ndiaye@somdop.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Diallo', prenom: 'Aissatou', email: 'a.diallo@somdop.com'],
                [nom: 'Fall', prenom: 'Abdoulaye', email: 'a.fall@somdop.com']
            ]],
            [nom: 'Coulibaly Industries', slug: 'coulibaly-industries', domaineEmail: 'coulibaly-industries.com', admins: [
                [nom: 'Coulibaly', prenom: 'Yacouba', email: 'y.coulibaly@coulibaly-industries.com', role: RolePersonnel.ADMIN],
                [nom: 'Konate', prenom: 'Aminata', email: 'a.konate@coulibaly-industries.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Sangare', prenom: 'Fatoumata', email: 'f.sangare@coulibaly-industries.com'],
                [nom: 'Ouattara', prenom: 'Koffi', email: 'k.ouattara@coulibaly-industries.com']
            ]]
        ]

        List<TypeEquipement> types = [
            save(new TypeEquipement(nom: 'Ordinateur')),
            save(new TypeEquipement(nom: 'Projecteur')),
            save(new TypeEquipement(nom: 'Imprimante')),
            save(new TypeEquipement(nom: 'Tablette')),
            save(new TypeEquipement(nom: 'Telephone'))
        ]

        etablissements.each { Map edata ->
            Etablissement etab = save(new Etablissement(nom: edata.nom, slug: edata.slug, domaineEmail: edata.domaineEmail, statut: 'ACTIF'))
            em.flush()
            RegleGestion.defaults(etab.domaineEmail).each { k, v ->
                em.persist(new RegleGestion(etablissement: etab, cle: k, valeur: v))
            }
            edata.admins.each { Map a ->
                save(new Personnel(nom: a.nom, prenom: a.prenom, email: a.email, motDePasse: demoAdminPassword, role: a.role, etablissement: etab))
            }
            edata.users.each { Map u ->
                save(new Personnel(nom: u.nom, prenom: u.prenom, email: u.email, motDePasse: demoUserPassword, role: RolePersonnel.USER, etablissement: etab))
            }

            Equipement eq1 = save(new Equipement(type: types[0], numeroSerie: "${edata.slug}-SN001", description: "Station fixe ${edata.nom}", etablissement: etab))
            Equipement eq2 = save(new Equipement(type: types[0], numeroSerie: "${edata.slug}-SN002", description: "PC portable ${edata.nom}", etat: EtatEquipement.DISPONIBLE, etablissement: etab))
            save(new Equipement(type: types[1], numeroSerie: "${edata.slug}-SN003", description: "Projecteur ${edata.nom}", etat: EtatEquipement.DISPONIBLE, etablissement: etab))
            save(new Equipement(type: types[2], numeroSerie: "${edata.slug}-SN004", description: "Imprimante ${edata.nom}", etat: EtatEquipement.DISPONIBLE, etablissement: etab))
            save(new Equipement(type: types[3], numeroSerie: "${edata.slug}-SN005", description: "Tablette ${edata.nom}", etat: EtatEquipement.DISPONIBLE, etablissement: etab))
            save(new Equipement(type: types[4], numeroSerie: "${edata.slug}-SN006", description: "Telephone ${edata.nom}", etat: EtatEquipement.DISPONIBLE, etablissement: etab))
            em.flush()

            List<Personnel> personnels = em.createQuery('select p from Personnel p where p.etablissement = :etab', Personnel)
                    .setParameter('etab', etab).resultList
            Personnel admin = personnels.find { it.role == RolePersonnel.ADMIN }
            List<Personnel> users = personnels.findAll { it.role == RolePersonnel.USER }

            affectationService.attribuer(eq1, users[0], admin)
            affectationService.attribuer(eq2, users[1], admin)

            save(new Signalement(equipement: eq2, personnel: users[0], type: TypeSignalement.PANNE, description: "L\'equipement ne s\'allume plus"))
            save(new Signalement(equipement: eq1, personnel: users[1], type: TypeSignalement.PROBLEME_FONCTIONNEL, description: "La porte USB ne fonctionne pas"))

            log.info('Etablissement cree: {} (id={}) avec {} admins, {} users', edata.nom, etab.id, edata.admins.size(), edata.users.size())
        }

        log.info('Jeu de donnees de demonstration cree pour {} etablissements', etablissements.size())
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