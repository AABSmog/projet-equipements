package proj.equipment

class TypeEquipement {
    static hasMany = [equipements: Equipement]
    String nom

    static constraints = {
        nom blank: false, unique: true
    }

    String toString() { nom }
}
