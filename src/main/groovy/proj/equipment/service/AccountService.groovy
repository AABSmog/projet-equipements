package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import proj.equipment.domain.RolePersonnel
import proj.equipment.dto.ApiModels

@Singleton
@CompileStatic
class AccountService {

    @PersistenceContext
    EntityManager em

    @Inject
    ValidationMessagesService validationMessagesService

    @Transactional
    Map<String, Object> register(String nom, String prenom, String email, String motDePasse) {
        Personnel p = new Personnel(
                nom: nom?.trim(),
                prenom: prenom?.trim(),
                email: email?.trim(),
                motDePasse: motDePasse,
                role: RolePersonnel.USER
        )
        String erreur = validationMessagesService.validatePersonnel(p, true)
        if (erreur) {
            return [success: false, message: erreur]
        }
        em.persist(p)
        String message = 'Compte cree avec succes. Connectez-vous avec votre email: ' + p.email
        return [success: true, message: message, user: ApiModels.user(p)]
    }
}