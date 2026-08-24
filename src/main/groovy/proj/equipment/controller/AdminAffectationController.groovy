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
@Controller('/api/admin/affectations')
@Transactional
class AdminAffectationController {

    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject AffectationService affectationService
    @Inject AuditService auditService
    @Inject CurrentUserService currentUserService
    @Inject SessionManager sessionManager

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '') String q,
                             @QueryValue(defaultValue = '0') Long typeId,
                             @QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeAffectations(q ?: null, typeId, Math.min(max, 100), Math.max(offset, 0))
    }

    @Get('/historique')
    Map<String, Object> historique(@QueryValue(defaultValue = '') String q,
                                   @QueryValue(defaultValue = '0') Long typeId,
                                   @QueryValue(defaultValue = '10') int max,
                                   @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeAffectations(q ?: null, typeId, Math.min(max, 100), Math.max(offset, 0))
    }

    @Post
    HttpResponse<Map<String, Object>> affecter(@Body Map<String, Object> body, HttpRequest<?> request) {
        Equipement equipement = lookup.equipementById(toLong(body.equipementId))
        Personnel personnel = lookup.userById(toLong(body.personnelId))
        Personnel operateur = currentUserService.get(request)
        Map<String, Object> result = affectationService.attribuer(equipement, personnel, operateur)
        if (result.success) {
            auditService.log(operateur, 'ATTRIBUTION', 'Affectation', null, "${equipement?.numeroSerie} -> ${personnel?.email}")
            return HttpUtil.ok(result)
        }
        HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }

    @Post('/{id}/retour')
    HttpResponse<Map<String, Object>> retour(@PathVariable Long id, @Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel operateur = currentUserService.get(request)
        Affectation affectation = lookup.affectationById(id)
        Map<String, Object> result = affectationService.restituer(affectation, body.raisonRetour as String)
        if (result.success) {
            auditService.log(operateur, 'RETOUR', 'Affectation', id, body.raisonRetour as String)
            return HttpUtil.ok(result)
        }
        HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }

    static Long toLong(Object value) {
        if (value == null || !value.toString().isLong()) return null
        value.toString().toLong()
    }
}