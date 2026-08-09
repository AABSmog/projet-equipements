package proj.equipment

class Equipement {
    TypeEquipement type
    String numeroSerie
    String description
    EtatEquipement etat = EtatEquipement.DISPONIBLE

    static belongsTo = [type: TypeEquipement]

    static constraints = {
        type nullable: false
        numeroSerie nullable: true, blank: false, unique: true, maxSize: 64
        description blank: false, maxSize: 500
        etat nullable: false, validator: { val, obj ->
            if (obj.id == null) {
                return val == EtatEquipement.DISPONIBLE ? true : 'equipement.etat.creation.ko'
            }
            if (val == EtatEquipement.AFFECTE) {
                if (!Affectation.findByEquipementAndDateRetourIsNull(obj)) {
                    return 'equipement.etat.affecte.requis'
                }
            } else if (val == EtatEquipement.DISPONIBLE || val == EtatEquipement.HORS_SERVICE) {
                if (Affectation.findByEquipementAndDateRetourIsNull(obj)) {
                    return val == EtatEquipement.DISPONIBLE ? 'equipement.etat.disponible.interdit' : 'equipement.etat.horsservice.interdit'
                }
            }
            return true
        }
    }

    def beforeInsert() {
        if (!numeroSerie) {
            numeroSerie = "SN-" + UUID.randomUUID().toString().take(8).toUpperCase()
        }
    }

    String toString() { "${type?.nom} #$id ($numeroSerie)" }
}
