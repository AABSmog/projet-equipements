package proj.equipment.service

import groovy.transform.CompileStatic
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Etablissement
import proj.equipment.domain.RegleGestion

@Singleton
@CompileStatic
class RegleGestionService {

    @PersistenceContext
    EntityManager em

    Map<String, String> getRegles(Etablissement etab) {
        if (!etab?.id) return RegleGestion.defaults()
        List<RegleGestion> list = em.createQuery('from RegleGestion where etablissement.id = :id', RegleGestion)
                .setParameter('id', etab.id).resultList as List<RegleGestion>
        if (!list) return RegleGestion.defaults(etab.domaineEmail ?: 'example.com')
        Map<String, String> m = [:]
        list.each { r -> m[r.cle] = r.valeur }
        // compléter les clés manquantes avec les défauts
        RegleGestion.defaults(etab.domaineEmail ?: 'example.com').each { k, v -> if (!m.containsKey(k)) m[k] = v }
        m
    }

    List<Map<String, Object>> listForApi(Etablissement etab) {
        Map<String, String> m = getRegles(etab)
        m.collect { k, v -> [cle: k, valeur: v, label: labelFor(k), type: typeFor(k)] as Map<String, Object> }
    }

    void saveAll(Etablissement etab, Map<String, String> regles) {
        regles.each { cle, valeur ->
            RegleGestion r = find(etab, cle)
            if (r) {
                r.valeur = valeur
                r.dateModif = new Date()
                em.merge(r)
            } else {
                em.persist(new RegleGestion(etablissement: etab, cle: cle, valeur: valeur))
            }
        }
    }

    void initDefaults(Etablissement etab) {
        Map<String, String> defs = RegleGestion.defaults(etab.domaineEmail ?: 'example.com')
        defs.each { k, v ->
            if (!find(etab, k)) {
                em.persist(new RegleGestion(etablissement: etab, cle: k, valeur: v))
            }
        }
    }

    String generateEmail(String prenom, String nom, Etablissement etab) {
        Map<String, String> regles = getRegles(etab)
        String pattern = regles['email.pattern'] ?: '{prenom}.{nom}@' + (etab?.domaineEmail ?: 'example.com')
        String domaine = regles['email.domaine'] ?: etab?.domaineEmail ?: 'example.com'
        // normaliser : sans accents, lower, espaces -> ''
        String p = normalize(prenom)
        String n = normalize(nom)
        String email = pattern.replace('{prenom}', p).replace('{nom}', n).replace('{domaine}', domaine)
        if (!email.contains('@')) email = "${p}.${n}@${domaine}"
        email.toLowerCase()
    }

    String validatePassword(String password, Etablissement etab) {
        if (!password) return 'Le mot de passe est obligatoire.'
        Map<String, String> r = getRegles(etab)
        int min = (r['password.minLength'] ?: '8').toInteger()
        if (password.size() < min) return "Le mot de passe doit contenir au moins ${min} caracteres."
        if (password.size() > 72) return 'Le mot de passe est trop long (72 caracteres max).'
        boolean maj = r['password.requireMaj'] != 'false'
        boolean minc = r['password.requireMin'] != 'false'
        boolean chiffre = r['password.requireChiffre'] != 'false'
        boolean symbole = r['password.requireSymbole'] == 'true'
        if (maj && !(password ==~ /.*[A-Z].*/)) return 'Le mot de passe doit contenir une majuscule.'
        if (minc && !(password ==~ /.*[a-z].*/)) return 'Le mot de passe doit contenir une minuscule.'
        if (chiffre && !(password ==~ /.*[0-9].*/)) return 'Le mot de passe doit contenir un chiffre.'
        if (symbole && !(password ==~ /.*[^a-zA-Z0-9].*/)) return 'Le mot de passe doit contenir un caractere special.'
        null
    }

    private RegleGestion find(Etablissement etab, String cle) {
        if (!etab?.id) return null
        List<RegleGestion> r = em.createQuery('from RegleGestion where etablissement.id = :eid and cle = :cle', RegleGestion)
                .setParameter('eid', etab.id).setParameter('cle', cle).setMaxResults(1).resultList as List<RegleGestion>
        r ? r[0] : null
    }

    private static String normalize(String s) {
        if (!s) return ''
        String v = s.toLowerCase().trim()
        v = java.text.Normalizer.normalize(v, java.text.Normalizer.Form.NFD).replaceAll('\\p{M}', '')
        v.replaceAll('[^a-z0-9]', '')
    }

    private static String labelFor(String cle) {
        switch (cle) {
            case 'email.pattern': return 'Pattern email'
            case 'email.domaine': return 'Domaine email'
            case 'password.minLength': return 'Longueur minimale MDP'
            case 'password.requireMaj': return 'Exiger majuscule'
            case 'password.requireMin': return 'Exiger minuscule'
            case 'password.requireChiffre': return 'Exiger chiffre'
            case 'password.requireSymbole': return 'Exiger symbole'
            case 'password.expirationJours': return 'Expiration MDP (jours)'
            case 'affectation.dureeMaxJours': return 'Durée max affectation (jours)'
            default: return cle
        }
    }

    private static String typeFor(String cle) {
        if (cle.startsWith('password.require')) return 'boolean'
        if (cle.contains('minLength') || cle.contains('Jours')) return 'number'
        return 'string'
    }
}
