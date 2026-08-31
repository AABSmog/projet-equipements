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
        creerEtablissementsDeDemonstration(principal)
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
        List<Map<String, Object>> etablissements = [
            [nom: 'SOMDOP Consulting', slug: 'somdop', domaineEmail: 'somdop.com', admins: [
                [nom: 'Diop', prenom: 'Mamadou', email: 'm.diop@somdop.com', role: RolePersonnel.ADMIN],
                [nom: 'Ndiaye', prenom: 'Fatou', email: 'f.ndiaye@somdop.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Diallo', prenom: 'Aissatou', email: 'a.diallo@somdop.com'],
                [nom: 'Fall', prenom: 'Abdoulaye', email: 'a.fall@somdop.com'],
                [nom: 'Sarr', prenom: 'Ndeye', email: 'n.sarr@somdop.com'],
                [nom: 'Traore', prenom: 'Issouf', email: 'i.traore@somdop.com']
            ]],
            [nom: 'Coulibaly Industries', slug: 'coulibaly-industries', domaineEmail: 'coulibaly-industries.com', admins: [
                [nom: 'Coulibaly', prenom: 'Yacouba', email: 'y.coulibaly@coulibaly-industries.com', role: RolePersonnel.ADMIN],
                [nom: 'Konate', prenom: 'Aminata', email: 'a.konate@coulibaly-industries.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Sangare', prenom: 'Fatoumata', email: 'f.sangare@coulibaly-industries.com'],
                [nom: 'Ouattara', prenom: 'Koffi', email: 'k.ouattara@coulibaly-industries.com'],
                [nom: 'Diao', prenom: 'Aida', email: 'a.diao@coulibaly-industries.com'],
                [nom: 'Camara', prenom: 'Mohamed', email: 'm.camara@coulibaly-industries.com']
            ]],
            [nom: 'Sylla Tech Solutions', slug: 'sylla-tech', domaineEmail: 'sylla-tech.com', admins: [
                [nom: 'Sylla', prenom: 'Hadja', email: 'h.sylla@sylla-tech.com', role: RolePersonnel.ADMIN],
                [nom: 'Ba', prenom: 'Marieme', email: 'm.ba@sylla-tech.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Diop', prenom: 'Moussa', email: 'm.diop@sylla-tech.com'],
                [nom: 'Ndiaye', prenom: 'Aminata', email: 'a.ndiaye@sylla-tech.com'],
                [nom: 'Diallo', prenom: 'Lamine', email: 'l.diallo@sylla-tech.com'],
                [nom: 'Fall', prenom: 'Mariam', email: 'm.fall@sylla-tech.com']
            ]],
            [nom: 'Ouattara Logistics', slug: 'ouattara-logistics', domaineEmail: 'ouattara-logistics.com', admins: [
                [nom: 'Ouattara', prenom: 'Koffi', email: 'k.ouattara@ouattara-logistics.com', role: RolePersonnel.ADMIN],
                [nom: 'Sangare', prenom: 'Lansana', email: 'l.sangare@ouattara-logistics.com', role: RolePersonnel.ADMIN]
            ], users: [
                [nom: 'Coulibaly', prenom: 'Adama', email: 'a.coulibaly@ouattara-logistics.com'],
                [nom: 'Konate', prenom: 'Ramatoulaye', email: 'r.konate@ouattara-logistics.com'],
                [nom: 'Diao', prenom: 'Aminata', email: 'a.diao@ouattara-logistics.com'],
                [nom: 'Camara', prenom: 'Ibrahima', email: 'i.camara@ouattara-logistics.com']
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