package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.MediaType
import proj.equipment.dto.ApiModels

@CompileStatic
@Controller('/api/meta')
class MetaController {

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> index() {
        [
                etatsEquipement: ApiModels.etats(),
                typesSignalement: ApiModels.typesSignalement(),
                roles: [
                        [value: 'ADMIN', label: 'Administrateur'],
                        [value: 'USER', label: 'Utilisateur']
                ]
        ]
    }
}