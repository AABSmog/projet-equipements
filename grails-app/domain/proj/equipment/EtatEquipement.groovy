package proj.equipment

enum EtatEquipement {
    DISPONIBLE("Disponible"),
    AFFECTE("Affecte"),
    EN_PANNE("En panne"),
    REPARE("Repare"),
    HORS_SERVICE("Hors service")

    final String label

    EtatEquipement(String label) { this.label = label }
}
