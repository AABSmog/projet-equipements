package proj.equipment.admin

import proj.equipment.Equipement
import proj.equipment.Affectation
import proj.equipment.Signalement
import proj.equipment.Personnel

class AdminController {

    static namespace = "admin"

    def index() {
        def totalEquipements = Equipement.count()
        def affectationsEnCours = Affectation.countByDateRetourIsNull()
        def signalementsOuverts = Signalement.count()
        def totalPersonnel = Personnel.count()
        [
            totalEquipements: totalEquipements,
            affectationsEnCours: affectationsEnCours,
            signalementsOuverts: signalementsOuverts,
            totalPersonnel: totalPersonnel
        ]
    }
}
