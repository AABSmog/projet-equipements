package proj.equipment

enum RolePersonnel {
    ADMIN("Administrateur"),
    USER("Utilisateur")

    final String label

    RolePersonnel(String label) { this.label = label }
}
