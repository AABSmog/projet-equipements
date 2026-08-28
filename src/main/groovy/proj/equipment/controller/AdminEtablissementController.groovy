package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.domain.Etablissement
import proj.equipment.service.EtablissementService

@CompileStatic
@Controller('/api/admin/etablissements')
class AdminEtablissementController {

    @Inject EtablissementService etablissementService

    @Get
    List<Map<String, Object>> list() {
        List<Map<String, Object>> result = []
        for (Etablissement e : etablissementService.listAll()) {
            result << ([id: e.id, nom: e.nom, slug: e.slug, domaineEmail: e.domaineEmail, statut: e.statut, dbUrl: e.dbUrl, dateCreation: e.dateCreation] as Map<String, Object>)
        }
        result
    }

    @Get('/{id}')
    HttpResponse<Map<String, Object>> show(@PathVariable Long id) {
        Etablissement e = etablissementService.findById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Etablissement introuvable')
        HttpUtil.ok([id: e.id, nom: e.nom, slug: e.slug, domaineEmail: e.domaineEmail, statut: e.statut, dbUrl: e.dbUrl] as Map<String, Object>)
    }

    @Get('/slug/{slug}')
    HttpResponse<Map<String, Object>> bySlug(@PathVariable String slug) {
        Etablissement e = etablissementService.findBySlug(slug)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Etablissement introuvable')
        HttpUtil.ok([id: e.id, nom: e.nom, slug: e.slug, domaineEmail: e.domaineEmail] as Map<String, Object>)
    }

    @Post('/wizard')
    HttpResponse<Map<String, Object>> wizard(@Body Map<String, Object> body, HttpRequest<?> request) {
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

    @Get('/preview-email')
    Map<String, Object> previewEmail(@QueryValue String prenom, @QueryValue String nom, @QueryValue(defaultValue = '') String etablissementId) {
        Etablissement etab = null
        if (etablissementId?.isLong()) etab = etablissementService.findById(etablissementId.toLong())
        if (!etab && prenom && nom) {
            // sans etablissement, preview basique
            String p = prenom.toLowerCase().replaceAll('[^a-z0-9]', '')
            String n = nom.toLowerCase().replaceAll('[^a-z0-9]', '')
            return [email: "${p}.${n}@example.com"] as Map<String, Object>
        }
        // si etab fourni, utiliser le service
        String email = etab ? new proj.equipment.service.RegleGestionService().generateEmail(prenom, nom, etab) : "${prenom}.${nom}@example.com"
        [email: email.toLowerCase()] as Map<String, Object>
    }
}
