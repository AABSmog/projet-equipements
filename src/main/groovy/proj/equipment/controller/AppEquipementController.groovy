package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Inject
import proj.equipment.domain.Affectation
import proj.equipment.domain.Equipement
import proj.equipment.domain.Personnel
import proj.equipment.dto.ApiModels
import proj.equipment.service.*

@CompileStatic
@Controller('/api/app/equipements')
@Transactional
class AppEquipementController {

    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject AffectationService affectationService
    @Inject AuditService auditService
    @Inject CurrentUserService currentUserService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset,
                             HttpRequest<?> request) {
        Personnel user = currentUserService.get(request)
        catalogService.mesEquipements(user, Math.min(max, 100), Math.max(offset, 0))
    }

    @Get('/{id}')
    HttpResponse<Map<String, Object>> show(@PathVariable Long id, HttpRequest<?> request) {
        Personnel user = currentUserService.get(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Affectation active = affectationService.findActiveAffectation(e, user)
        if (!active) return HttpUtil.erreur(HttpStatus.FORBIDDEN, 'Cet equipement ne vous est pas affecte')
        Map m = ApiModels.equipement(e)
        m.affectation = ApiModels.affectation(active)
        m.signalements = catalogService.signalementsEquipement(e)
        HttpUtil.ok(m)
    }

    @Post('/{id}/retour')
    HttpResponse<Map<String, Object>> retour(@PathVariable Long id, @Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel user = currentUserService.get(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Map<String, Object> result = affectationService.restituer(e, user, body.raisonRetour as String)
        if (result.success) {
            auditService.log(user, 'RETOUR', 'Equipement', e.id, body.raisonRetour as String)
            return HttpUtil.ok(result)
        }
        HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }
}