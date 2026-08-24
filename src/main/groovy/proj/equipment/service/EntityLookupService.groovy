package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*

@Singleton
@CompileStatic
class EntityLookupService {

    @PersistenceContext
    EntityManager em

    @Transactional
    Personnel userById(Long id) {
        id != null ? em.find(Personnel, id) : null
    }

    @Transactional
    Equipement equipementById(Long id) {
        id != null ? em.find(Equipement, id) : null
    }

    @Transactional
    TypeEquipement typeById(Long id) {
        id != null ? em.find(TypeEquipement, id) : null
    }

    @Transactional
    Affectation affectationById(Long id) {
        id != null ? em.find(Affectation, id) : null
    }

    @Transactional
    Signalement signalementById(Long id) {
        id != null ? em.find(Signalement, id) : null
    }

    @Transactional
    Equipement nouveauEquipement() {
        new Equipement()
    }
}