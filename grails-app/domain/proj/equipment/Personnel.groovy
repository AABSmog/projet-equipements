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
        email blank: false, unique: true, email: true, maxSize: 254
        nom blank: false, maxSize: 100
        prenom blank: false, maxSize: 100
        motDePasse blank: false, minSize: 8, maxSize: 72, validator: { val, obj ->
            if (!val || val.startsWith('\$2')) {
                return true
            }
            if (!(val ==~ /.*[a-z].*/) || !(val ==~ /.*[A-Z].*/) || !(val ==~ /.*[0-9].*/)) {
                return 'motDePasse.force.insuffisante'
            }
            return true
        }
    }

    static mapping = {
        email index: 'personnel_email_idx'
        role index: 'personnel_role_idx'
    }

    String toString() { "$prenom $nom ($email)" }
}
