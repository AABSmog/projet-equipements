package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import proj.equipment.domain.Personnel
import proj.equipment.domain.Signalement
import proj.equipment.dto.ApiModels
import proj.equipment.service.*

@CompileStatic
@Controller('/api/admin/signalements')
@Transactional
class AdminSignalementController {

    @PersistenceContext EntityManager em
    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject AuditService auditService
    @Inject CurrentUserService currentUserService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '') String q,
                             @QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeSignalements(q ?: null, Math.min(max, 100), Math.max(offset, 0))
    }

    @Delete('/{id}')
    HttpResponse<Map<String, Object>> delete(@PathVariable Long id, HttpRequest<?> request) {
        Personnel operateur = currentUserService.get(request)
        Signalement s = lookup.signalementById(id)
        if (!s) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Signalement introuvable')
        String info = s.personnel?.email
        em.remove(s)
        auditService.log(operateur, 'DELETE', 'Signalement', id, info)
        HttpUtil.ok([message: 'Signalement supprime'])
    }
}