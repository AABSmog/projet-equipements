package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.service.CatalogService

@CompileStatic
@Controller('/api/admin/recherche')
class AdminRechercheController {

    @Inject CatalogService catalogService

    @Get('/equipements')
    List<Map<String, Object>> equipements(@QueryValue(defaultValue = '') String q,
                                          @QueryValue(defaultValue = '') String typeId) {
        Long tid = typeId && typeId.isLong() ? typeId.toLong() : null
        catalogService.rechercherEquipementsDisponibles(q ?: null, tid)
    }

    @Get('/personnels')
    List<Map<String, Object>> personnels(@QueryValue(defaultValue = '') String q) {
        catalogService.rechercherPersonnel(q ?: null)
    }
}