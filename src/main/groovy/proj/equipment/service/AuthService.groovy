package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import org.mindrot.jbcrypt.BCrypt

@Singleton
@CompileStatic
class AuthService {

    @PersistenceContext
    EntityManager em

    @Transactional(readOnly = true)
    Personnel authenticate(String email, String password) {
        return authenticate(email, password, null)
    }

    @Transactional(readOnly = true)
    Personnel authenticate(String email, String password, Long etablissementId) {
        if (!email || !password) return null
        String jpql = 'from Personnel where lower(email) = :e'
        if (etablissementId) jpql += ' and etablissement.id = :eid'
        def query = em.createQuery(jpql, Personnel).setParameter('e', email.trim().toLowerCase())
        if (etablissementId) query.setParameter('eid', etablissementId)
        List<Personnel> users = query.setMaxResults(1).resultList
        if (!users) return null
        Personnel user = users[0]
        try {
            BCrypt.checkpw(password, user.motDePasse) ? user : null
        } catch (IllegalArgumentException ex) {
            null
        }
    }
}