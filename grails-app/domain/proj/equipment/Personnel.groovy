package proj.equipment

class Personnel {
    def beforeInsert() {
        motDePasse = org.mindrot.jbcrypt.BCrypt.hashpw(motDePasse, org.mindrot.jbcrypt.BCrypt.gensalt())
    }
    def beforeUpdate() {
        if (isDirty('motDePasse')) {
            motDePasse = org.mindrot.jbcrypt.BCrypt.hashpw(motDePasse, org.mindrot.jbcrypt.BCrypt.gensalt())
        }
    }
    String nom
    String prenom
    String email
    String motDePasse
    RolePersonnel role = RolePersonnel.USER

    static constraints = {
        email blank: false, unique: true, email: true
        nom blank: false
        prenom blank: false
        motDePasse blank: false
    }

    String toString() { "$prenom $nom ($email)" }
}
