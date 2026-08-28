package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.TypedQuery
import proj.equipment.domain.*
import proj.equipment.dto.ApiModels

@Singleton
@CompileStatic
class CatalogService {

    @PersistenceContext
    EntityManager em

    @Transactional(readOnly = true)
    Map<String, Object> listeEquipements(String q, String etat, int max, int offset) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' lower(e.numeroSerie) like :p or lower(e.description) like :p or lower(t.nom) like :p ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        if (etat) {
            if (q) where.append(' and ')
            where.append(' e.etat = :etat ')
            params.etat = EtatEquipement.valueOf(etat)
        }
        String cond = where.toString()
        String order = ' order by e.id asc'

        def items = valParams(em.createQuery("select distinct e from Equipement e left join e.type t ${cond ? 'where ' + cond : ''}${order}", Equipement), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Equipement>

        long total = singleLong("select count(distinct e) from Equipement e left join e.type t ${cond ? 'where ' + cond : ''}", params)

        Map<Long, Affectation> affectePar = [:]
        if (items) {
            List<Long> ids = items*.id
            List<Affectation> actives = em.createQuery(
                    'select distinct a from Affectation a join fetch a.personnel p left join fetch a.attribuePar where a.dateRetour is null and a.equipement.id in :ids', Affectation)
                    .setParameter('ids', ids)
                    .resultList as List<Affectation>
            actives.each { a ->
                if (!affectePar.containsKey(a.equipement.id)) {
                    affectePar[a.equipement.id] = a
                }
            }
        }

        [
                items       : items.collect { e ->
                    Map m = ApiModels.equipement(e)
                    Affectation a = affectePar.get(e.id)
                    m.affecte = a ? ApiModels.personnel(a.personnel) : null
                    m
                },
                total       : total,
                max         : max,
                offset      : offset,
                types       : listeTypes(),
                etats       : ApiModels.etats()
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> listeAffectations(String q, Long typeId, int max, int offset) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' (lower(p.nom) like :p or lower(p.prenom) like :p or lower(ap.nom) like :p or lower(ap.prenom) like :p or lower(a.infoEquipement) like :p or lower(e.numeroSerie) like :p or lower(t.nom) like :p) ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        if (typeId) {
            if (where.length()) where.append(' and ')
            where.append(' t.id = :typeId ')
            params.typeId = typeId
        }
        String cond = where.toString()
        String order = ' order by a.dateAffectation desc'

        def items = valParams(em.createQuery(
                "select distinct a from Affectation a join fetch a.personnel p left join fetch a.equipement e left join fetch a.attribuePar ap left join e.type t ${cond ? 'where ' + cond : ''}${order}", Affectation), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Affectation>

        long total = singleLong("select count(distinct a) from Affectation a join a.personnel p left join a.equipement e left join a.attribuePar ap left join e.type t ${cond ? 'where ' + cond : ''}", params)

        [
                items : items.collect { a -> ApiModels.affectation(a) },
                total : total,
                max   : max,
                offset: offset,
                types : listeTypes()
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> listePersonnels(String q, int max, int offset) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' lower(p.nom) like :p or lower(p.prenom) like :p or lower(p.email) like :p ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        String cond = where.toString()
        def items = valParams(em.createQuery("from Personnel p ${cond ? 'where ' + cond : ''} order by p.nom asc, p.prenom asc", Personnel), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Personnel>
        long total = singleLong("select count(p) from Personnel p ${cond ? 'where ' + cond : ''}", params)

        List<Long> ids = items*.id
        Map<Long, Long> nbAff = ids ? counts("select a.personnel.id, count(a) from Affectation a where a.personnel.id in :ids group by a.personnel.id", ids) : [:]
        Map<Long, Long> nbSig = ids ? counts("select s.personnel.id, count(s) from Signalement s where s.personnel.id in :ids group by s.personnel.id", ids) : [:]

        [
                items : items.collect { p ->
                    Map m = ApiModels.personnel(p)
                    m.nbAffectations = nbAff.get(p.id, 0L)
                    m.nbSignalements = nbSig.get(p.id, 0L)
                    m
                },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> listeSignalements(String q, int max, int offset) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' (lower(s.description) like :p or (s.equipement is not null and lower(e.numeroSerie) like :p)) ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        String cond = where.toString()
        def items = valParams(em.createQuery(
                "select s from Signalement s left join fetch s.personnel p left join fetch s.equipement e ${cond ? 'where ' + cond : ''} order by s.dateCreated desc", Signalement), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Signalement>
        long total = singleLong("select count(s) from Signalement s left join s.equipement e ${cond ? 'where ' + cond : ''}", params)

        [
                items : items.collect { s -> ApiModels.signalement(s) },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> listeAudit(String q, int max, int offset) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' lower(a.utilisateur) like :p or lower(a.action) like :p or lower(a.cible) like :p or lower(a.details) like :p ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        String cond = where.toString()
        def items = valParams(em.createQuery("from AuditLog a ${cond ? 'where ' + cond : ''} order by a.dateCreated desc", AuditLog), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<AuditLog>
        long total = singleLong("select count(a) from AuditLog a ${cond ? 'where ' + cond : ''}", params)

        [
                items : items.collect { it -> ApiModels.audit(it) },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    List<Map<String, Object>> listeTypes() {
        List<TypeEquipement> types = em.createQuery('from TypeEquipement order by nom asc', TypeEquipement).resultList as List<TypeEquipement>
        types.collect { t -> ApiModels.typeEquipement(t) }
    }

    @Transactional(readOnly = true)
    Map<String, Object> listeTypesPaginee(String q, int max, int offset) {
        Map<String, Object> params = [:]
        String cond = ''
        if (q) {
            cond = ' where lower(t.nom) like :p '
            params.p = '%' + q.toLowerCase() + '%'
        }
        List<TypeEquipement> items = valParams(em.createQuery("from TypeEquipement t ${cond} order by t.nom asc", TypeEquipement), params)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<TypeEquipement>
        long total = singleLong("select count(t) from TypeEquipement t ${cond}", params)
        [
                items : items.collect { t -> ApiModels.typeEquipement(t) },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> statsAdministration() {
        [
                totalEquipements    : singleLong('select count(e) from Equipement e', [:]),
                affectationsEnCours : singleLong('select count(a) from Affectation a where a.dateRetour is null', [:]),
                signalementsOuverts : singleLong('select count(s) from Signalement s', [:]),
                totalPersonnel      : singleLong('select count(p) from Personnel p', [:])
        ]
    }

    @Transactional(readOnly = true)
    List<Map<String, Object>> rechercherEquipementsDisponibles(String q, Long typeId) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder(' e.etat = :etat ')
        params.etat = EtatEquipement.DISPONIBLE
        if (typeId) {
            where.append(' and t.id = :tid ')
            params.tid = typeId
        }
        if (q) {
            where.append(' and (lower(e.numeroSerie) like :p or lower(e.description) like :p or lower(t.nom) like :p) ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        def items = valParams(em.createQuery(
                "select e from Equipement e join e.type t where ${where.toString()} order by e.numeroSerie asc", Equipement), params)
                .setMaxResults(20)
                .resultList as List<Equipement>
        items.collect { e ->
            [id: e.id, label: "${e.type?.nom ?: ''} - ${e.numeroSerie}".replaceAll(/^ - /, ''), numeroSerie: e.numeroSerie] as Map<String, Object>
        }
    }

    @Transactional(readOnly = true)
    List<Map<String, Object>> rechercherPersonnel(String q) {
        Map<String, Object> params = [:]
        StringBuilder where = new StringBuilder()
        if (q) {
            where.append(' where (lower(p.nom) like :p or lower(p.prenom) like :p) ')
            params.p = '%' + q.toLowerCase() + '%'
        }
        def items = valParams(em.createQuery("from Personnel p ${where} order by p.nom asc, p.prenom asc", Personnel), params)
                .setMaxResults(20)
                .resultList as List<Personnel>
        items.collect { p -> [id: p.id, label: p.prenom + ' ' + p.nom] as Map<String, Object> }
    }

    @Transactional(readOnly = true)
    Map<String, Object> mesEquipements(Personnel user, int max, int offset) {
        List<Affectation> actives = em.createQuery(
                'select a from Affectation a left join fetch a.equipement e left join fetch a.equipement.type where a.personnel.id = :uid and a.dateRetour is null order by a.dateAffectation desc', Affectation)
                .setParameter('uid', user.id)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Affectation>
        long total = singleLong('select count(a) from Affectation a where a.personnel.id = :uid and a.dateRetour is null', [uid: user.id])
        [
                items : actives.collect { a -> ApiModels.affectation(a) },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    Map<String, Object> mesSignalements(Personnel user, int max, int offset) {
        List<Signalement> items = em.createQuery(
                'select s from Signalement s left join fetch s.equipement e where s.personnel.id = :uid order by s.dateCreated desc', Signalement)
                .setParameter('uid', user.id)
                .setMaxResults(max)
                .setFirstResult(offset)
                .resultList as List<Signalement>
        long total = singleLong('select count(s) from Signalement s where s.personnel.id = :uid', [uid: user.id])
        [
                items : items.collect { s -> ApiModels.signalement(s) },
                total : total,
                max   : max,
                offset: offset
        ]
    }

    @Transactional(readOnly = true)
    List<Map<String, Object>> signalementsEquipement(Equipement equipement) {
        List<Signalement> items = em.createQuery(
                'select s from Signalement s left join fetch s.personnel p where s.equipement.id = :eid order by s.dateCreated desc', Signalement)
                .setParameter('eid', equipement.id)
                .resultList as List<Signalement>
        items.collect { s -> ApiModels.signalement(s) }
    }

    
    private <T> jakarta.persistence.TypedQuery<T> valParams(jakarta.persistence.TypedQuery<T> query, Map<String, Object> params) {
        params.each { k, v -> query.setParameter((String) k, v) }
        query
    }

    private long singleLong(String jpql, Map<String, Object> params) {
        TypedQuery<Long> q = em.createQuery(jpql, Long)
        params.each { k, v -> q.setParameter((String) k, v) }
        q.singleResult ?: 0L
    }

    private Map<Long, Long> counts(String jpql, List<Long> ids) {
        Map<Long, Long> result = [:]
        em.createQuery(jpql, Object[].class)
                .setParameter('ids', ids)
                .resultList.each { Object[] row ->
                    Object[] tuple = (row.length == 1 && row[0] instanceof Object[]) ? (Object[]) row[0] : row
                    result.put(toLong(tuple[0]), toLong(tuple[1]))
                }
        result
    }

    private static Long toLong(Object value) {
        value instanceof Number ? ((Number) value).longValue() : (value != null ? Long.parseLong(value.toString()) : null)
    }
}