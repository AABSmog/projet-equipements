package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import proj.equipment.domain.RolePersonnel
import proj.equipment.domain.Affectation
import proj.equipment.domain.Signalement
import proj.equipment.service.*

@CompileStatic
@Controller('/api/admin/personnels')
@Transactional
class AdminPersonnelController {

    @PersistenceContext EntityManager em
    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject CurrentUserService currentUserService
    @Inject ValidationMessagesService validationMessagesService
    @Inject AuditService auditService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '') String q,
                             @QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset) {
        catalogService.listePersonnels(q ?: null, Math.min(max, 100), Math.max(offset, 0))
    }

    @Get('/{id}')
    HttpResponse<Map<String, Object>> show(@PathVariable Long id) {
        Personnel p = lookup.userById(id)
        if (!p) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Personnel introuvable')
        HttpUtil.ok(proj.equipment.dto.ApiModels.personnel(p))
    }

    @Post
    HttpResponse<Map<String, Object>> create(@Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel p = new Personnel(
                nom: body.nom as String,
                prenom: body.prenom as String,
                email: body.email as String,
                motDePasse: body.motDePasse as String,
                role: parseRole(body.role as String)
        )
        String erreur = validationMessagesService.validatePersonnel(p, true)
        if (erreur) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, erreur)
        em.persist(p)
        auditService.log(currentUserService.get(request), 'CREATE', 'Personnel', p.id, p.email)
        HttpUtil.ok(proj.equipment.dto.ApiModels.personnel(p))
    }

    @Put('/{id}')
    HttpResponse<Map<String, Object>> update(@PathVariable Long id, @Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel p = lookup.userById(id)
        if (!p) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Personnel introuvable')
        Personnel original = p
        p.nom = body.nom as String
        p.prenom = body.prenom as String
        p.email = body.email as String
        p.role = parseRole(body.role as String)
        String motDePasse = body.motDePasse as String
        if (motDePasse) {
            p.motDePasse = motDePasse
        }
        String erreur = validationMessagesService.validatePersonnel(p, false)
        if (erreur) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, erreur)
        em.merge(p)
        auditService.log(currentUserService.get(request), 'UPDATE', 'Personnel', p.id, p.email)
        HttpUtil.ok(proj.equipment.dto.ApiModels.personnel(p))
    }

    @Delete('/{id}')
    HttpResponse<Map<String, Object>> delete(@PathVariable Long id, HttpRequest<?> request) {
        Personnel operateur = currentUserService.get(request)
        Personnel p = lookup.userById(id)
        if (!p) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Personnel introuvable')
        if (operateur && id == operateur.id) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Impossible de supprimer votre propre compte')
        }
        if (p.role == RolePersonnel.ADMIN) {
            long nbAdmins = em.createQuery('select count(x) from Personnel x where x.role = :r', Long)
                    .setParameter('r', RolePersonnel.ADMIN).singleResult
            if (nbAdmins <= 1) {
                return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Impossible de supprimer le dernier administrateur')
            }
        }
        long nbAffectations = em.createQuery('select count(a) from Affectation a where a.personnel.id = :id', Long)
                .setParameter('id', id).singleResult
        long nbSignalements = em.createQuery('select count(s) from Signalement s where s.personnel.id = :id', Long)
                .setParameter('id', id).singleResult
        long nbAttribuePar = em.createQuery('select count(a) from Affectation a where a.attribuePar.id = :id', Long)
                .setParameter('id', id).singleResult
        if (nbAffectations > 0 || nbSignalements > 0 || nbAttribuePar > 0) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, "Suppression impossible : historique d'affectations ou de signalements existant")
        }
        em.remove(p)
        auditService.log(operateur, 'DELETE', 'Personnel', id, p.email)
        HttpUtil.ok([message: 'Personnel supprime'])
    }

    private RolePersonnel parseRole(String role) {
        try {
            role ? RolePersonnel.valueOf(role) : RolePersonnel.USER
        } catch (IllegalArgumentException e) {
            RolePersonnel.USER
        }
    }
}