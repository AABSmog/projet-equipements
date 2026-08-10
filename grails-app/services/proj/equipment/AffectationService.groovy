package proj.equipment

import grails.gorm.transactions.Transactional

@Transactional
class AffectationService {

    def validationMessagesService

    Map attribuer(Equipement equipement, Personnel personnel, Personnel operateur) {
        if (!equipement) {
            return [success: false, message: "Equipement introuvable"]
        }
        if (!personnel) {
            return [success: false, message: "Personnel introuvable"]
        }
        if (equipement.etat != EtatEquipement.DISPONIBLE) {
            return [success: false, message: "L'equipement ${equipement.type?.nom} (${equipement.numeroSerie}) n'est pas disponible"]
        }
        if (Affectation.findByEquipementAndPersonnelAndDateRetourIsNull(equipement, personnel)) {
            return [success: false, message: "Cet equipement est deja affecte a ${personnel.prenom} ${personnel.nom}"]
        }
        def result = [success: false]
        Affectation.withTransaction { status ->
            def affectation = new Affectation(
                equipement: equipement,
                personnel: personnel,
                attribuePar: operateur,
                dateAffectation: new Date()
            )
            if (!affectation.save(flush: true)) {
                result.message = validationMessagesService?.message(affectation) ?: "L'affectation n'a pas pu etre creee"
                status.setRollbackOnly()
                return
            }
            equipement.etat = EtatEquipement.AFFECTE
            if (!equipement.save(flush: true)) {
                result.message = validationMessagesService?.message(equipement) ?: "La mise a jour de l'equipement a echoue"
                status.setRollbackOnly()
                return
            }
            result.success = true
            result.message = "Equipement affecte a ${personnel.prenom} ${personnel.nom}"
        }
        return result
    }

    Map restituer(Affectation affectation, String raison) {
        if (!affectation) {
            return [success: false, message: "Affectation introuvable"]
        }
        if (affectation.dateRetour) {
            return [success: false, message: "Cette affectation a deja ete cloturee"]
        }
        affectation.dateRetour = new Date()
        affectation.raisonRetour = raison ?: "Retour volontaire"
        if (!affectation.save(flush: true)) {
            return [success: false, message: validationMessagesService?.message(affectation) ?: "Le retour n'a pas pu etre enregistre"]
        }
        if (affectation.equipement) {
            affectation.equipement.etat = EtatEquipement.DISPONIBLE
            affectation.equipement.save(flush: true)
        }
        return [success: true, message: "Retour enregistre"]
    }

    Map restituer(Equipement equipement, Personnel personnel, String raison) {
        if (!equipement) {
            return [success: false, message: "Equipement introuvable"]
        }
        if (!personnel) {
            return [success: false, message: "Personnel introuvable"]
        }
        def affectation = Affectation.findByEquipementAndPersonnelAndDateRetourIsNull(equipement, personnel)
        if (!affectation) {
            return [success: false, message: "Aucune affectation active pour cet equipement"]
        }
        return restituer(affectation, raison)
    }

    Map desaffecter(Equipement equipement) {
        if (!equipement) {
            return [success: false, message: "Equipement introuvable"]
        }
        if (equipement.etat != EtatEquipement.AFFECTE) {
            return [success: false, message: "Cet equipement n'est pas affecte"]
        }
        def affectation = Affectation.findByEquipementAndDateRetourIsNull(equipement)
        if (!affectation) {
            return [success: false, message: "Aucune affectation active pour cet equipement"]
        }
        affectation.dateRetour = new Date()
        affectation.raisonRetour = "Desaffecte par l'administrateur"
        affectation.save(flush: true)
        equipement.etat = EtatEquipement.DISPONIBLE
        equipement.save(flush: true)
        return [success: true, message: "Equipement desaffecte"]
    }

    Map declasser(Equipement equipement) {
        if (!equipement) {
            return [success: false, message: "Equipement introuvable"]
        }
        if (equipement.etat == EtatEquipement.AFFECTE) {
            def affectation = Affectation.findByEquipementAndDateRetourIsNull(equipement)
            if (affectation) {
                affectation.dateRetour = new Date()
                affectation.raisonRetour = "Equipement declare hors service"
                affectation.infoEquipement = "${equipement.type?.nom} - ${equipement.numeroSerie} (declasse)"
                affectation.save(flush: true)
            }
        }
        equipement.etat = EtatEquipement.HORS_SERVICE
        equipement.save(flush: true)
        return [success: true, message: "Equipement declare hors service"]
    }
}
