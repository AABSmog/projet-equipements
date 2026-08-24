package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Affectation
import proj.equipment.domain.Equipement
import proj.equipment.domain.EtatEquipement
import proj.equipment.domain.Personnel

@Singleton
@CompileStatic
class AffectationService {

    @PersistenceContext
    EntityManager em

    @Inject
    ValidationMessagesService validationMessagesService

    @Transactional
    Map<String, Object> attribuer(Equipement equipement, Personnel personnel, Personnel operateur) {
        if (!equipement) {
            return [success: false, message: 'Equipement introuvable']
        }
        if (!personnel) {
            return [success: false, message: 'Personnel introuvable']
        }
        if (equipement.etat != EtatEquipement.DISPONIBLE) {
            return [success: false, message: "L'equipement ${equipement.type?.nom} (${equipement.numeroSerie}) n'est pas disponible"]
        }
        if (findActiveAffectation(equipement, personnel)) {
            return [success: false, message: "Cet equipement est deja affecte a ${personnel.prenom} ${personnel.nom}"]
        }
        Affectation affectation = new Affectation(
                equipement: equipement,
                personnel: personnel,
                attribuePar: operateur,
                dateAffectation: new Date()
        )
        String erreur = validationMessagesService.validateAffectation(affectation)
        if (erreur) {
            return [success: false, message: erreur]
        }
        em.persist(affectation)
        equipement.etat = EtatEquipement.AFFECTE
        em.merge(equipement)
        return [success: true, message: "Equipement affecte a ${personnel.prenom} ${personnel.nom}"]
    }

    @Transactional
    Map<String, Object> restituer(Affectation affectation, String raison) {
        if (!affectation) {
            return [success: false, message: 'Affectation introuvable']
        }
        if (affectation.dateRetour) {
            return [success: false, message: 'Cette affectation a deja ete cloturee']
        }
        affectation.dateRetour = new Date()
        affectation.raisonRetour = raison ?: 'Retour volontaire'
        String erreur = validationMessagesService.validateAffectation(affectation)
        if (erreur) {
            return [success: false, message: erreur]
        }
        em.merge(affectation)
        if (affectation.equipement) {
            Equipement eq = affectation.equipement
            eq.etat = EtatEquipement.DISPONIBLE
            em.merge(eq)
        }
        return [success: true, message: 'Retour enregistre']
    }

    @Transactional
    Map<String, Object> restituer(Equipement equipement, Personnel personnel, String raison) {
        if (!equipement) {
            return [success: false, message: 'Equipement introuvable']
        }
        if (!personnel) {
            return [success: false, message: 'Personnel introuvable']
        }
        Affectation affectation = findActiveAffectation(equipement, personnel)
        if (!affectation) {
            return [success: false, message: 'Aucune affectation active pour cet equipement']
        }
        return restituer(affectation, raison)
    }

    @Transactional
    Map<String, Object> desaffecter(Equipement equipement) {
        if (!equipement) {
            return [success: false, message: 'Equipement introuvable']
        }
        if (equipement.etat != EtatEquipement.AFFECTE) {
            return [success: false, message: "Cet equipement n'est pas affecte"]
        }
        Affectation affectation = findActiveAffectation(equipement)
        if (!affectation) {
            return [success: false, message: 'Aucune affectation active pour cet equipement']
        }
        affectation.dateRetour = new Date()
        affectation.raisonRetour = "Desaffecte par l'administrateur"
        em.merge(affectation)
        equipement.etat = EtatEquipement.DISPONIBLE
        em.merge(equipement)
        return [success: true, message: 'Equipement desaffecte']
    }

    @Transactional
    Map<String, Object> declasser(Equipement equipement) {
        if (!equipement) {
            return [success: false, message: 'Equipement introuvable']
        }
        Affectation affectation = findActiveAffectation(equipement)
        if (affectation) {
            affectation.dateRetour = new Date()
            affectation.raisonRetour = 'Equipement declare hors service'
            affectation.infoEquipement = "${equipement.type?.nom} - ${equipement.numeroSerie} (declasse)"
            em.merge(affectation)
        }
        equipement.etat = EtatEquipement.HORS_SERVICE
        em.merge(equipement)
        return [success: true, message: 'Equipement declare hors service']
    }

    Affectation findActiveAffectation(Equipement equipement, Personnel personnel = null) {
        if (!equipement?.id) return null
        String q = 'from Affectation where equipement.id = :id and dateRetour is null'
        if (personnel?.id) {
            q += ' and personnel.id = :pid'
        }
        jakarta.persistence.TypedQuery<Affectation> query = em.createQuery(q, Affectation).setParameter('id', equipement.id)
        if (personnel?.id) {
            query.setParameter('pid', personnel.id)
        }
        List<Affectation> r = query.setMaxResults(1).resultList
        r ? r[0] : null
    }

    Affectation findById(Long id) {
        em.find(Affectation, id)
    }
}