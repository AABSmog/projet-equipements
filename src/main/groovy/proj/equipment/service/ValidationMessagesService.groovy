package proj.equipment.service

import groovy.transform.CompileStatic
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*

/**
 * Traductions des erreurs de validation en messages francais. Reprend les
 * codes du projet Grails d'origine.
 */
@Singleton
@CompileStatic
class ValidationMessagesService {

    @PersistenceContext
    EntityManager em

    String validatePersonnel(Personnel p, boolean creation) {
        String value = p.motDePasse
        boolean plainText = value && !value.startsWith('$2')
        if (!p.nom?.trim()) return 'Le nom est obligatoire.'
        if (p.nom?.size() > 100) return 'Le nom est trop long.'
        if (!p.prenom?.trim()) return 'Le prenom est obligatoire.'
        if (p.prenom?.size() > 100) return 'Le prenom est trop long.'
        if (!p.email?.trim()) return "L'adresse email est obligatoire."
        if (p.email?.size() > 254) return "L'adresse email est trop longue."
        if (!(p.email ==~ /^[^@\s]+@[^@\s]+\.[^@\s]+$/)) return "L'adresse email n'est pas valide."
        Personnel existing = findByEmailIgnoreCase(p.email?.trim())
        if (existing && existing.id != p.id) return 'Cet email est deja utilise.'
        if (creation || plainText) {
            String pw = value ?: p.motDePasse
            if (!pw?.trim()) return 'Le mot de passe est obligatoire.'
            if (pw?.size() < 8) return 'Le mot de passe doit contenir au moins 8 caracteres.'
            if (pw?.size() > 72) return 'Le mot de passe est trop long (72 caracteres max).'
            if (!(pw ==~ /.*[a-z].*/) || !(pw ==~ /.*[A-Z].*/) || !(pw ==~ /.*[0-9].*/)) {
                return 'Le mot de passe doit contenir 8 caracteres minimum, avec au moins une minuscule, une majuscule et un chiffre.'
            }
        }
        null
    }

    String validateEquipement(Equipement e, boolean creation) {
        if (!e.type) return 'Veuillez selectionner un type.'
        if (!e.description?.trim()) return 'La description est obligatoire.'
        if (e.description?.size() > 500) return 'La description est trop longue.'
        if (e.numeroSerie) {
            if (e.numeroSerie?.size() > 64) return 'Le numero de serie est trop long.'
            Long count = em.createQuery('select count(x.id) from Equipement x where lower(x.numeroSerie) = :s and x.id <> :id', Long)
                    .setParameter('s', e.numeroSerie.trim().toLowerCase())
                    .setParameter('id', e.id ?: -1L)
                    .singleResult
            if (count > 0) return 'Ce numero de serie est deja utilise.'
        }
        if (creation && e.etat != EtatEquipement.DISPONIBLE) {
            return 'Un nouvel equipement doit etre cree a l\'etat disponible.'
        }
        if (e.etat == EtatEquipement.AFFECTE) {
            if (!affectationActive(e)) return 'Cet equipement ne peut pas etre marque affecte sans affectation active.'
        } else if (e.etat == EtatEquipement.DISPONIBLE) {
            if (affectationActive(e)) return 'Impossible de rendre disponible un equipement encore affecte.'
        } else if (e.etat == EtatEquipement.HORS_SERVICE) {
            if (affectationActive(e)) return 'Impossible de classer hors service un equipement encore affecte.'
        }
        null
    }

    String validateTypeEquipement(TypeEquipement t) {
        if (!t.nom?.trim()) return 'Le nom est obligatoire.'
        if (t.nom?.size() > 255) return 'Le nom est trop long.'
        TypeEquipement existing = typeByNomIgnoreCase(t.nom?.trim())
        if (existing && existing.id != t.id) return "Ce type d'equipement existe deja."
        null
    }

    String validateSignalement(Signalement s) {
        if (!s.type) return 'Le type de signalement est obligatoire.'
        if (!s.description?.trim()) return 'La description est obligatoire.'
        if (s.description?.size() > 1000) return 'La description est trop longue.'
        null
    }

    String validateAffectation(Affectation a) {
        if (!a.personnel) return 'Le personnel est obligatoire.'
        if (!a.dateAffectation) return "La date d'affectation est obligatoire."
        if (a.dateRetour && a.dateAffectation && a.dateRetour.before(a.dateAffectation)) {
            return "La date de retour ne peut pas etre anterieure a la date d'affectation."
        }
        null
    }

    private boolean affectationActive(Equipement e) {
        if (!e.id) return false
        Long c = em.createQuery('select count(x) from Affectation x where x.equipement.id = :id and x.dateRetour is null', Long)
                .setParameter('id', e.id).singleResult
        c > 0
    }

    private Personnel findByEmailIgnoreCase(String email) {
        List<Personnel> r = em.createQuery('from Personnel where lower(email) = :e', Personnel)
                .setParameter('e', email.toLowerCase())
                .setMaxResults(1)
                .resultList
        r ? r[0] : null
    }

    private TypeEquipement typeByNomIgnoreCase(String nom) {
        List<TypeEquipement> r = em.createQuery('from TypeEquipement where lower(nom) = :n', TypeEquipement)
                .setParameter('n', nom.toLowerCase())
                .setMaxResults(1)
                .resultList
        r ? r[0] : null
    }
}