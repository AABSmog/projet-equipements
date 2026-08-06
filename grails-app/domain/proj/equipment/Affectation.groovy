package proj.equipment

class Affectation {
    Equipement equipement
    Personnel personnel
    Date dateAffectation
    Date dateRetour
    String raisonRetour
    String infoEquipement

    static belongsTo = [personnel: Personnel]

    static constraints = {
        dateRetour nullable: true
        raisonRetour nullable: true
        equipement nullable: true
        infoEquipement nullable: true
    }

    String toString() { "Affectation: ${equipement ?: infoEquipement} -> ${personnel}" }
}
