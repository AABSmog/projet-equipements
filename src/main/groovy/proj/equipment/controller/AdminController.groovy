package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.inject.Inject
import proj.equipment.service.CatalogService

@CompileStatic
@Controller('/api/admin')
class AdminController {

    @Inject CatalogService catalogService

    @Get('/stats')
    Map<String, Object> stats() {
        catalogService.statsAdministration()
    }
}