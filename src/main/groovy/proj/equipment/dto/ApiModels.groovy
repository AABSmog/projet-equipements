package proj.equipment.dto

import groovy.transform.CompileStatic
import proj.equipment.domain.*

@CompileStatic
class ApiModels {

    static Map<String, Object> user(Personnel p) {
        if (!p) return null
        [
                id          : p.id,
                email       : p.email,
                nom         : p.nom,
                prenom      : p.prenom,
                role        : p.role?.name(),
                roleLabel   : p.role?.label,
                etablissement: p.etablissement ? [id: p.etablissement.id, nom: p.etablissement.nom, slug: p.etablissement.slug] : null
        ]
    }

    static Map<String, Object> typeEquipement(TypeEquipement t) {
        if (!t) return null
        [id: t.id, nom: t.nom]
    }

    static Map<String, Object> equipement(Equipement e) {
        if (!e) return null
        [
                id          : e.id,
                numeroSerie : e.numeroSerie,
                description : e.description,
                etat        : e.etat?.name(),
                etatLabel   : e.etat?.label,
                type        : typeEquipement(e.type)
        ]
    }

    static Map<String, Object> personnel(Personnel p) {
        if (!p) return null
        [
                id    : p.id,
                email : p.email,
                nom   : p.nom,
                prenom: p.prenom,
                role  : p.role?.name(),
                roleLabel: p.role?.label
        ]
    }

    static Map<String, Object> personnelComplet(Personnel p) {
        if (!p) return null
        Map m = personnel(p)
        m.nbAffectations = p.id == null ? 0L : null
        m.nbSignalements = null
        m
    }

    static Map<String, Object> affectation(Affectation a) {
        if (!a) return null
        [
                id            : a.id,
                dateAffectation: a.dateAffectation,
                dateRetour    : a.dateRetour,
                raisonRetour  : a.raisonRetour,
                infoEquipement: a.infoEquipement,
                equipement    : equipement(a.equipement),
                personnel     : personnel(a.personnel),
                attribuePar   : personnel(a.attribuePar)
        ]
    }

    static Map<String, Object> signalement(Signalement s) {
        if (!s) return null
        [
                id         : s.id,
                dateCreated: s.dateCreated,
                type       : s.type?.name(),
                typeLabel  : s.type?.label,
                description: s.description,
                infoEquipement: s.infoEquipement,
                equipement : equipement(s.equipement),
                personnel  : personnel(s.personnel)
        ]
    }

    static Map<String, Object> audit(AuditLog l) {
        if (!l) return null
        [
                id        : l.id,
                dateCreated: l.dateCreated,
                utilisateur: l.utilisateur,
                action    : l.action,
                cible     : l.cible,
                cibleId   : l.cibleId,
                details   : l.details
        ]
    }

    static List<Map<String, String>> etats() {
        List<Map<String, String>> result = []
        EtatEquipement.values().each { e ->
            result.add([value: e.name(), label: e.label])
        }
        result
    }

    static List<Map<String, String>> typesSignalement() {
        List<Map<String, String>> result = []
        TypeSignalement.values().each { t ->
            result.add([value: t.name(), label: t.label])
        }
        result
    }
}