package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.service.CatalogService

@CompileStatic
@Controller('/api/admin')
class AdminTypeController {

    @Inject CatalogService catalogService

    @Get('/types')
    List<Map<String, Object>> list() {
        catalogService.listeTypes()
    }

    @Get('/types/list')
    Map<String, Object> listPaginated(@QueryValue(defaultValue = '') String q,
                                      @QueryValue(defaultValue = '20') int max,
                                      @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeTypesPaginee(q ?: null, Math.min(max, 100), Math.max(offset, 0))
    }

    // Les types se creent via l'option "Autre..." du formulaire equipement :
    // leur creation / modification / suppression directe est desactivee.
    @Post('/types')
    HttpResponse<Map<String, Object>> create() {
        message()
    }

    @Put('/types/{id}')
    HttpResponse<Map<String, Object>> update(Long id) {
        message()
    }

    @Delete('/types/{id}')
    HttpResponse<Map<String, Object>> delete(Long id) {
        message()
    }

    private static HttpResponse<Map<String, Object>> message() {
        HttpResponse.badRequest([error: 'Les types se creent via l\'option "Autre..." du formulaire equipement.']) as HttpResponse<Map<String, Object>>
    }
}