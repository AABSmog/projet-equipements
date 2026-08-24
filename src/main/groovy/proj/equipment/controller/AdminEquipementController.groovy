package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.data.exceptions.EmptyResultException
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import io.micronaut.transaction.annotation.Transactional
import proj.equipment.domain.*
import proj.equipment.dto.ApiModels
import proj.equipment.service.*

@CompileStatic
@Controller('/api/admin/equipements')
@Transactional
class AdminEquipementController {

    @PersistenceContext EntityManager em
    @Inject CatalogService catalogService
    @Inject EntityLookupService lookup
    @Inject CurrentUserService currentUserService
    @Inject ValidationMessagesService validationMessagesService
    @Inject AffectationService affectationService
    @Inject AuditService auditService

    @Get
    Map<String, Object> list(@QueryValue(defaultValue = '') String q,
                             @QueryValue(defaultValue = '') String etat,
                             @QueryValue(defaultValue = '10') int max,
                             @QueryValue(defaultValue = '0') int offset) {
        catalogService.listeEquipements(q ?: null, etat ?: null, normalizeMax(max, 100), Math.max(offset, 0))
    }

    @Get('/{id}')
    HttpResponse<Map<String, Object>> show(@PathVariable Long id) {
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Map m = ApiModels.equipement(e)
        Affectation active = affectationService.findActiveAffectation(e)
        if (active) {
            m.affecte = ApiModels.affectation(active)
        }
        m.signalements = catalogService.signalementsEquipement(e)
        HttpUtil.ok(m)
    }

    @Get('/{id}/declasser-confirm')
    HttpResponse<Map<String, Object>> declasserConfirm(@PathVariable Long id) {
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Affectation active = (e.etat == EtatEquipement.AFFECTE) ? affectationService.findActiveAffectation(e) : null
        HttpUtil.ok([
                equipement: ApiModels.equipement(e),
                affectation: active ? ApiModels.affectation(active) : null
        ])
    }

    @Post
    HttpResponse<Map<String, Object>> create(@Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel operateur = currentUser(request)
        TypeEquipement type = resolveType(body)
        if (!type) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Veuillez selectionner un type')

        Equipement e = new Equipement(
                type: type,
                numeroSerie: body.numeroSerie as String,
                description: body.description as String,
                etat: EtatEquipement.DISPONIBLE
        )
        String erreur = validationMessagesService.validateEquipement(e, true)
        if (erreur) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, erreur)
        }
        em.persist(e)
        auditService.log(operateur, 'CREATE', 'Equipement', e.id, "${e.type?.nom} - ${e.numeroSerie}")
        HttpUtil.ok(ApiModels.equipement(e))
    }

    @Put('/{id}')
    HttpResponse<Map<String, Object>> update(@PathVariable Long id, @Body Map<String, Object> body, HttpRequest<?> request) {
        Personnel operateur = currentUser(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')

        TypeEquipement type = resolveType(body)
        if (!type) return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Veuillez selectionner un type')

        e.type = type
        e.description = body.description as String
        e.numeroSerie = body.numeroSerie as String
        String etat = body.etat as String
        if (etat) {
            try {
                e.etat = EtatEquipement.valueOf(etat)
            } catch (IllegalArgumentException ex) {
                return HttpUtil.erreur(HttpStatus.BAD_REQUEST, 'Etat invalide')
            }
        }
        String erreur = validationMessagesService.validateEquipement(e, false)
        if (erreur) {
            return HttpUtil.erreur(HttpStatus.BAD_REQUEST, erreur)
        }
        em.merge(e)
        auditService.log(operateur, 'UPDATE', 'Equipement', e.id, "etat=${e.etat}")
        HttpUtil.ok(ApiModels.equipement(e))
    }

    @Post('/{id}/desaffecter')
    HttpResponse<Map<String, Object>> desaffecter(@PathVariable Long id, HttpRequest<?> request) {
        Personnel operateur = currentUser(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Map<String, Object> result = affectationService.desaffecter(e)
        if (result.success) {
            auditService.log(operateur, 'DESAFFECTATION', 'Equipement', e.id, e.numeroSerie)
            return HttpUtil.ok(result)
        }
        HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }

    @Post('/{id}/declasser')
    HttpResponse<Map<String, Object>> declasser(@PathVariable Long id, HttpRequest<?> request) {
        Personnel operateur = currentUser(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')
        Map<String, Object> result = affectationService.declasser(e)
        if (result.success) {
            auditService.log(operateur, 'DECLASSEMENT', 'Equipement', e.id, e.numeroSerie)
            return HttpUtil.ok(result)
        }
        HttpUtil.erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }

    @Delete('/{id}')
    HttpResponse<Map<String, Object>> delete(@PathVariable Long id, HttpRequest<?> request) {
        Personnel operateur = currentUser(request)
        Equipement e = lookup.equipementById(id)
        if (!e) return HttpUtil.erreur(HttpStatus.NOT_FOUND, 'Equipement introuvable')

        String info = "${e.type?.nom} - ${e.numeroSerie} (supprime)"
        List<Affectation> affs = em.createQuery('from Affectation where equipement.id = :id', Affectation)
                .setParameter('id', e.id).resultList as List<Affectation>
        affs.each { a ->
            if (!a.dateRetour) a.dateRetour = new Date()
            a.infoEquipement = info
            a.equipement = null
            em.merge(a)
        }
        List<Signalement> sigs = em.createQuery('from Signalement where equipement.id = :id', Signalement)
                .setParameter('id', e.id).resultList as List<Signalement>
        sigs.each { s ->
            s.infoEquipement = info
            s.equipement = null
            em.merge(s)
        }
        em.remove(e)
        auditService.log(operateur, 'DELETE', 'Equipement', id, info)
        HttpUtil.ok([message: 'Equipement supprime'])
    }

    private TypeEquipement resolveType(Map<String, Object> body) {
        String typeId = body.typeId?.toString()
        if (typeId == 'autre') {
            String nom = body.nouveauType as String
            if (!nom?.trim()) return null
            TypeEquipement existant = findTypeByNom(nom.trim())
            if (existant) return existant
            TypeEquipement nouveau = new TypeEquipement(nom: nom.trim())
            String erreur = validationMessagesService.validateTypeEquipement(nouveau)
            if (erreur) return null
            em.persist(nouveau)
            return nouveau
        }
        if (!typeId?.isLong()) return null
        return lookup.typeById(typeId.toLong())
    }

    private TypeEquipement findTypeByNom(String nom) {
        List<TypeEquipement> r = em.createQuery('from TypeEquipement where lower(nom) = :n', TypeEquipement)
                .setParameter('n', nom.toLowerCase()).setMaxResults(1).resultList
        r ? r[0] : null
    }

    private Personnel currentUser(HttpRequest<?> request) {
        currentUserService.get(request)
    }

    private static int normalizeMax(int max, int cap) {
        Math.min(max, cap)
    }
}