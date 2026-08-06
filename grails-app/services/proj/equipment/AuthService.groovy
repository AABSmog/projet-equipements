package proj.equipment

class AuthService {

    Personnel authenticate(String email, String password) {
        Personnel.findByEmail(email)?.with { user ->
            org.mindrot.jbcrypt.BCrypt.checkpw(password, user.motDePasse) ? user : null
        }
    }
}
