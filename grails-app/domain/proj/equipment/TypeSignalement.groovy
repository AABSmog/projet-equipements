package proj.equipment

enum TypeSignalement {
    PANNE("Panne"),
    PROBLEME_FONCTIONNEL("Probleme fonctionnel"),
    CASSE("Casse"),
    AUTRE("Autre")

    final String label

    TypeSignalement(String label) { this.label = label }
}
