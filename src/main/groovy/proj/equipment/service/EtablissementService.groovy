package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Etablissement
import proj.equipment.domain.Personnel
import proj.equipment.domain.RolePersonnel

@Singleton
@CompileStatic
class EtablissementService {

    @PersistenceContext
    EntityManager em

    @Inject ValidationMessagesService validationMessagesService
    @Inject RegleGestionService regleGestionService

    List<Etablissement> listAll() {
        em.createQuery('from Etablissement order by nom asc', Etablissement).resultList as List<Etablissement>
    }

    Etablissement findById(Long id) {
        if (!id) return null
        em.find(Etablissement, id)
    }

    Etablissement findBySlug(String slug) {
        if (!slug) return null
        List<Etablissement> r = em.createQuery('from Etablissement where lower(slug) = :s', Etablissement)
                .setParameter('s', slug.toLowerCase()).setMaxResults(1).resultList as List<Etablissement>
        r ? r[0] : null
    }

    String slugify(String nom) {
        if (!nom) return ''
        String s = java.text.Normalizer.normalize(nom.toLowerCase().trim(), java.text.Normalizer.Form.NFD).replaceAll('\\p{M}', '')
        s = s.replaceAll('[^a-z0-9]+', '-').replaceAll('(^-|-$)', '')
        s.take(80)
    }

    @Transactional
    Map<String, Object> createWizard(Map<String, Object> payload, Personnel operateur) {
        // payload: { etablissement: {nom, domaineEmail}, admins: [{nom,prenom,email,motDePasse}], employes: [...], regles: {...} }
        Map etabMap = payload.etablissement as Map
        String nom = etabMap?.nom?.toString()?.trim()
        String domaine = etabMap?.domaineEmail?.toString()?.trim() ?: 'example.com'
        String slug = etabMap?.slug?.toString()?.trim() ?: slugify(nom)
        if (!nom) return [success: false, message: "Le nom de l'etablissement est obligatoire"]
        if (!slug) return [success: false, message: "Le slug est obligatoire"]
        if (findBySlug(slug)) return [success: false, message: "Un etablissement avec ce slug existe deja"]
        // Création etablissement (single-DB mode : dbUrl null)
        Etablissement etab = new Etablissement(
                nom: nom,
                slug: slug,
                domaineEmail: domaine,
                dbUrl: "jdbc:postgresql://127.0.0.1:5432/equipments_${slug.replaceAll('-', '_')}",
                statut: 'ACTIF'
        )
        em.persist(etab)
        em.flush()

        // Règles par défaut + surcharges
        regleGestionService.initDefaults(etab)
        Map regles = payload.regles as Map
        if (regles) {
            Map<String, String> toSave = [:]
            regles.each { k, v -> toSave[k.toString()] = v.toString() }
            regleGestionService.saveAll(etab, toSave)
        }

        // Admins
        List admins = payload.admins as List ?: []
        if (!admins) return [success: false, message: "Au moins un compte administrateur est requis"]
        List<Personnel> createdAdmins = []
        for (Object o : admins) {
            Map m = o as Map
            String email = m.email?.toString()?.trim()
            if (!email) {
                // auto-génération via règle
                email = regleGestionService.generateEmail(m.prenom as String, m.nom as String, etab)
            }
            Personnel p = new Personnel(
                    nom: m.nom as String,
                    prenom: m.prenom as String,
                    email: email,
                    motDePasse: m.motDePasse as String,
                    role: RolePersonnel.ADMIN,
                    etablissement: etab
            )
            String err = validationMessagesService.validatePersonnel(p, true)
            if (err) {
                throw new IllegalArgumentException("Admin ${m.prenom} ${m.nom} : ${err}")
            }
            // vérification password via règles
            String pwErr = regleGestionService.validatePassword(p.motDePasse, etab)
            if (pwErr) throw new IllegalArgumentException("Admin ${email} : ${pwErr}")
            em.persist(p)
            createdAdmins << p
        }

        // Employés (optionnel)
        List employes = payload.employes as List ?: []
        List<Personnel> createdEmps = []
        for (Object o : employes) {
            Map m = o as Map
            if (!m.nom && !m.prenom && !m.email) continue
            String email = m.email?.toString()?.trim()
            if (!email) email = regleGestionService.generateEmail(m.prenom as String, m.nom as String, etab)
            Personnel p = new Personnel(
                    nom: m.nom as String,
                    prenom: m.prenom as String,
                    email: email,
                    motDePasse: (m.motDePasse as String) ?: 'User1234',
                    role: RolePersonnel.USER,
                    etablissement: etab
            )
            String err = validationMessagesService.validatePersonnel(p, true)
            if (err) throw new IllegalArgumentException("Employe ${m.prenom} ${m.nom} : ${err}")
            String pwErr = regleGestionService.validatePassword(p.motDePasse, etab)
            if (pwErr) throw new IllegalArgumentException("Employe ${email} : ${pwErr}")
            em.persist(p)
            createdEmps << p
        }

        em.flush()
        [
            success     : true,
            message     : "Etablissement '${etab.nom}' cree : ${createdAdmins.size()} admin(s), ${createdEmps.size()} employe(s)",
            etablissement: [id: etab.id, nom: etab.nom, slug: etab.slug, domaineEmail: etab.domaineEmail],
            admins      : createdAdmins.collect { [id: it.id, email: it.email, nom: it.nom, prenom: it.prenom] },
            employes    : createdEmps.collect { [id: it.id, email: it.email] }
        ]
    }
}
