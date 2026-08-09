package proj.equipment

class TypeEquipement {
    static hasMany = [equipements: Equipement]
    String nom

    static constraints = {
        nom blank: false, unique: true, validator: { val, obj ->
            if (val) {
                def existing = TypeEquipement.findByNomIlike(val?.trim())
                if (existing && existing.id != obj.id) {
                    return 'type.nom.existe'
                }
            }
            return true
        }
    }

    String toString() { nom }
}
