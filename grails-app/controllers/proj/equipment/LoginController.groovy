package proj.equipment

class LoginController {

    def authService
    def validationMessagesService

    def index() {
        if (session.user) {
            redirectBasedOnRole()
        }
    }

    def attempt() {
        def user = authService.authenticate(params.email, params.password)
        if (user) {
            session.user = user
            redirectBasedOnRole()
        } else {
            flash.error = "Email ou mot de passe incorrect"
            render(view: "/login/index")
        }
    }

    def logout() {
        session.user = null
        session.invalidate()
        redirect(controller: "login", action: "index")
    }

    def register() {
        render(view: "/register/index", model: [personnel: new Personnel(params)])
    }

    def saveRegistration() {
        def personnel = new Personnel(
            nom: params.nom,
            prenom: params.prenom,
            email: params.email,
            motDePasse: params.motDePasse,
            role: RolePersonnel.USER
        )
        Personnel.withTransaction { status ->
            if (personnel.save(flush: true)) {
                flash.success = "Compte cree avec succes. Votre email de connexion est: ${params.email}"
                session.user = personnel
                redirect(uri: "/app")
            } else {
                flash.error = validationMessagesService.message(personnel) ?: "Erreur lors de la creation du compte"
                render(view: "/register/index", model: [personnel: personnel])
            }
        }
    }

    private redirectBasedOnRole() {
        if (session.user.role == RolePersonnel.ADMIN) {
            redirect(uri: "/admin")
        } else {
            redirect(uri: "/app")
        }
    }
}
