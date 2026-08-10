package proj.equipment.admin

import proj.equipment.Equipement
import proj.equipment.TypeEquipement
import proj.equipment.EtatEquipement
import proj.equipment.Affectation
import proj.equipment.Signalement
import grails.gorm.transactions.Transactional

@Transactional
class EquipementController {

    static namespace = "admin"

    def validationMessagesService
    def affectationService

    def index() {
        redirect(action: "list")
    }

    def list() {
        def q = params.q
        def etatFiltre = params.etat
        def results = Equipement.createCriteria().list {
            if (q) {
                or {
                    ilike("numeroSerie", "%${q}%")
                    ilike("description", "%${q}%")
                    type { ilike("nom", "%${q}%") }
                }
            }
            if (etatFiltre) {
                eq("etat", EtatEquipement.valueOf(etatFiltre))
            }
            order("id", "asc")
        }
        [equipementList: results, typeEquipementList: TypeEquipement.list(sort: "nom")]
    }

    def create() {
        [equipement: new Equipement(params), typeEquipementList: TypeEquipement.list(sort: "nom")]
    }

    def save() {
        def typeId = params['type.id']
        TypeEquipement typeCree = null
        if (typeId == 'autre') {
            def nom = params.nouveauType?.trim()
            if (!nom) {
                flash.error = "Veuillez saisir le nom du nouveau type"
                redirect(action: "create")
                return
            }
            def typeExist = TypeEquipement.findByNomIlike(nom)
            if (typeExist) {
                typeId = typeExist.id.toString()
            } else {
                typeCree = new TypeEquipement(nom: nom)
                if (!typeCree.save(flush: true)) {
                    flash.error = "Erreur lors de la creation du type"
                    redirect(action: "create")
                    return
                }
                typeId = typeCree.id.toString()
            }
        }
        if (!typeId) {
            flash.error = "Veuillez selectionner un type"
            redirect(action: "create")
            return
        }
        params['type.id'] = typeId
        def equipement = new Equipement(params)
        if (equipement.save(flush: true)) {
            flash.success = "Equipement cree"
            redirect(action: "list")
        } else {
            if (typeCree) {
                typeCree.delete(flush: true)
            }
            flash.error = validationMessagesService.message(equipement) ?: "Erreur lors de la creation"
            render(view: "create", model: [equipement: equipement, typeEquipementList: TypeEquipement.list(sort: "nom")])
        }
    }

    def edit() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        [equipement: equipement, typeEquipementList: TypeEquipement.list(sort: "nom")]
    }

    def update() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def typeId = params['type.id']
        TypeEquipement typeCree = null
        if (typeId == 'autre') {
            def nom = params.nouveauType?.trim()
            if (!nom) {
                flash.error = "Veuillez saisir le nom du nouveau type"
                redirect(action: "edit", id: equipement.id)
                return
            }
            def typeExist = TypeEquipement.findByNomIlike(nom)
            if (typeExist) {
                typeId = typeExist.id.toString()
            } else {
                typeCree = new TypeEquipement(nom: nom)
                if (!typeCree.save(flush: true)) {
                    flash.error = "Erreur lors de la creation du type"
                    redirect(action: "edit", id: equipement.id)
                    return
                }
                typeId = typeCree.id.toString()
            }
        }
        if (!typeId) {
            flash.error = "Veuillez selectionner un type"
            redirect(action: "edit", id: equipement.id)
            return
        }
        params['type.id'] = typeId
        equipement.properties = params
        if (equipement.save(flush: true)) {
            flash.success = "Equipement mis a jour"
            redirect(action: "list")
        } else {
            if (typeCree) {
                typeCree.delete(flush: true)
            }
            flash.error = validationMessagesService.message(equipement) ?: "Erreur lors de la mise a jour"
            render(view: "edit", model: [equipement: equipement, typeEquipementList: TypeEquipement.list(sort: "nom")])
        }
    }

    def desaffecter() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def result = affectationService.desaffecter(equipement)
        if (result.success) {
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }

    def declasserConfirm() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def affectation = equipement.etat == EtatEquipement.AFFECTE ? Affectation.findByEquipementAndDateRetourIsNull(equipement) : null
        [equipement: equipement, affectation: affectation]
    }

    def declasser() {
        def equipement = Equipement.get(params.id)
        if (!equipement) {
            flash.error = "Equipement introuvable"
            redirect(action: "list")
            return
        }
        def result = affectationService.declasser(equipement)
        if (result.success) {
            flash.success = result.message
        } else {
            flash.error = result.message
        }
        redirect(action: "list")
    }

    def delete() {
        def equipement = Equipement.get(params.id)
        if (equipement) {
            def info = "${equipement.type?.nom} - ${equipement.numeroSerie} (supprime)"
            Affectation.findAllByEquipement(equipement).each { aff ->
                if (!aff.dateRetour) {
                    aff.dateRetour = new Date()
                }
                aff.infoEquipement = info
                aff.equipement = null
                aff.save()
            }
            Signalement.findAllByEquipement(equipement).each { sig ->
                sig.infoEquipement = info
                sig.equipement = null
                sig.save()
            }
            equipement.delete(flush: true)
            flash.success = "Equipement supprime"
        }
        redirect(action: "list")
    }
}
