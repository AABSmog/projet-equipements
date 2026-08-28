package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.domain.Etablissement
import proj.equipment.service.EtablissementService
import proj.equipment.service.RegleGestionService

@CompileStatic
@Controller('/api/admin/etablissements/{etablissementId}/regles')
class AdminRegleController {

    @Inject EtablissementService etablissementService
    @Inject RegleGestionService regleGestionService

    @Get
    HttpResponse<Map<String, Object>> list(@PathVariable Long etablissementId) {
        Etablissement etab = etablissementService.findById(etablissementId)
        if (!etab) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Etablissement introuvable')
        List<Map<String, Object>> regles = regleGestionService.listForApi(etab)
        HttpUtil.ok([etablissement: [id: etab.id, nom: etab.nom, slug: etab.slug], regles: regles] as Map<String, Object>)
    }

    @Put
    HttpResponse<Map<String, Object>> update(@PathVariable Long etablissementId, @Body Map<String, Object> body) {
        Etablissement etab = etablissementService.findById(etablissementId)
        if (!etab) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Etablissement introuvable')
        Map regles = body.regles as Map ?: body as Map
        Map<String, String> toSave = [:]
        regles.each { k, v -> toSave[k.toString()] = v.toString() }
        // validation basique
        if (toSave.containsKey('password.minLength')) {
            try {
                int v = toSave['password.minLength'].toInteger()
                if (v < 6 || v > 32) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Longueur minimale doit etre entre 6 et 32')
            } catch (NumberFormatException e) {
                return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Longueur minimale invalide')
            }
        }
        regleGestionService.saveAll(etab, toSave)
        HttpUtil.ok([message: 'Regles mises a jour', regles: regleGestionService.listForApi(etab)] as Map<String, Object>)
    }

    @Get('/preview-email')
    Map<String, Object> preview(@PathVariable Long etablissementId, @QueryValue String prenom, @QueryValue String nom) {
        Etablissement etab = etablissementService.findById(etablissementId)
        if (!etab) return [email: ''] as Map<String, Object>
        String email = regleGestionService.generateEmail(prenom ?: '', nom ?: '', etab)
        [email: email, pattern: regleGestionService.getRegles(etab)['email.pattern']] as Map<String, Object>
    }
}
