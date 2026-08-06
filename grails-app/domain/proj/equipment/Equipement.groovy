package proj.equipment

class Equipement {
    TypeEquipement type
    String numeroSerie
    String description
    EtatEquipement etat = EtatEquipement.DISPONIBLE

    static belongsTo = [type: TypeEquipement]

    static constraints = {
        numeroSerie blank: false, unique: true
        description blank: false, maxSize: 500
    }

    def beforeInsert() {
        if (!numeroSerie) {
            numeroSerie = "SN-" + UUID.randomUUID().toString().take(8).toUpperCase()
        }
    }

    String toString() { "${type?.nom} #$id ($numeroSerie)" }
}
