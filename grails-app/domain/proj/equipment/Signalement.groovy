package proj.equipment

class Signalement {
    Date dateCreated
    Equipement equipement
    Personnel personnel
    TypeSignalement type
    String description
    String infoEquipement

    static belongsTo = [personnel: Personnel]

    static mapping = {
        equipement index: 'idx_signalement_equipement'
        personnel index: 'idx_signalement_personnel'
        dateCreated index: 'idx_signalement_date'
    }

    static constraints = {
        description blank: false, maxSize: 1000
        type nullable: false
        personnel nullable: false
        equipement nullable: true
        infoEquipement nullable: true
    }

    String toString() { "Signalement: ${type?.label} - ${equipement ?: infoEquipement}" }
}
