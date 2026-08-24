package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.*
import proj.equipment.dto.ApiModels
import proj.equipment.service.*

@CompileStatic
@Controller('/api/app/signalements')
@Transactional
class AppSignalementController {

    @PersistenceContext EntityManager em
    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject AffectationService affectationService
    @Inject ValidationMessagesService validationMessagesService
    @Inject AuditService auditService
    @Inject CurrentUserService currentUserService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset,
                             HttpRequest<?> request) {
        Personnel personnel = currentUserService.get(request)
        catalogService.mesSignalements(personnel, Math.min(max, 100), Math.max(offset, 0))
    }

    @Get('/creation')
    HttpResponse<Map<String, Object>> creation(@QueryValue Long equipementId, HttpRequest<?> request) {
        Personnel user = currentUserService.get(request)
        Equipement e = lookup.equipementById(equipementId)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Affectation active = affectationService.findActiveAffectation(e, user)
        if (!active) return HttpUtil.erreur(HttpStatus.FORBIDDEN, 'Cet equipement ne vous est pas affecte')
        HttpUtil.ok([
                equipement: ApiModels.equipement(e),
                types     : ApiModels.typesSignalement()
        ])
    }

    @Post
    HttpResponse<Map<String, Object>> save(@Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel user = currentUserService.get(request)
        Equipement e = lookup.equipementById(AdminAffectationController.toLong(body.equipementId))
        if (!e) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Equipement introuvable')
        Affectation active = affectationService.findActiveAffectation(e, user)
        if (!active) return HttpUtil.erreur(HttpStatus.FORBIDDEN, 'Cet equipement ne vous est pas affecte')

        String description = body.description as String
        if (!description?.trim()) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'La description est obligatoire.')
        }
        TypeSignalement type
        try {
            type = body.type ? TypeSignalement.valueOf(body.type as String) : null
        } catch (IllegalArgumentException ex) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Type de signalement invalide')
        }
        Signalement s = new Signalement(
                equipement: e,
                personnel: user,
                type: type,
                description: description?.trim()
        )
        String erreur = validationMessagesService.validateSignalement(s)
        if (erreur) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, erreur)
        em.persist(s)
        auditService.log(user, 'SIGNALEMENT', 'Equipement', e.id, description)
        HttpUtil.ok(ApiModels.signalement(s))
    }
}