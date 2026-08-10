package proj.equipment

class Affectation {
    Equipement equipement
    Personnel personnel
    Personnel attribuePar
    Date dateAffectation
    Date dateRetour
    String raisonRetour
    String infoEquipement

    static belongsTo = [personnel: Personnel]

    static mapping = {
        equipement index: 'idx_affect_equipement'
        personnel index: 'idx_affect_personnel'
        dateAffectation index: 'idx_affect_date'
        dateRetour index: 'idx_affect_retour'
    }

    static constraints = {
        personnel nullable: false
        dateAffectation nullable: false
        attribuePar nullable: true
        dateRetour nullable: true, validator: { val, obj ->
            if (val && obj.dateAffectation && val.before(obj.dateAffectation)) {
                return 'affectation.dateRetour.anterieur'
            }
            return true
        }
        raisonRetour nullable: true
        equipement nullable: true
        infoEquipement nullable: true
    }

    String toString() { "Affectation: ${equipement ?: infoEquipement} -> ${personnel}" }
}
