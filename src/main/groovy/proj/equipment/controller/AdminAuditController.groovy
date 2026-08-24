package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import jakarta.inject.Inject
import proj.equipment.service.CatalogService

@CompileStatic
@Controller('/api/admin/audit')
class AdminAuditController {

    @Inject CatalogService catalogService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '') String q,
                             @QueryValue(defaultValue = '20') int max,
                             @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeAudit(q ?: null, Math.min(max, 200), Math.max(offset, 0))
    }
}