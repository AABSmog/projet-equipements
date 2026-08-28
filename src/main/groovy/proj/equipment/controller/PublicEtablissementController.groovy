package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.service.EtablissementService

@CompileStatic
@Controller('/api/etablissements')
class PublicEtablissementController {

    @Inject EtablissementService etablissementService

    @Get
    List<Map<String, Object>> list() {
        etablissementService.listAll().collect { e ->
            [id: e.id, nom: e.nom, slug: e.slug, domaineEmail: e.domaineEmail, statut: e.statut] as Map<String, Object>
        }
    }

    @Get('/{id}')
    HttpResponse<Map<String, Object>> show(@PathVariable Long id) {
        def e = etablissementService.findById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Etablissement introuvable')
        HttpUtil.ok([id: e.id, nom: e.nom, slug: e.slug, domaineEmail: e.domaineEmail] as Map<String, Object>)
    }

    @Post('/wizard')
    HttpResponse<Map<String, Object>> wizard(@Body Map<String, Object> body) {
        try {
            Map<String, Object> result = etablissementService.createWizard(body, null)
            if (result.success) return HttpUtil.ok(result)
            HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
        } catch (IllegalArgumentException ex) {
            HttpUtil.erreur(HttpStatus.BAD_REQUEST, ex.message)
        } catch (Exception ex) {
            HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Erreur lors de la creation : ' + ex.message)
        }
    }
}
