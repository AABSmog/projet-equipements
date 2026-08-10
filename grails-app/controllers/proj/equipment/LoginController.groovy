package proj.equipment

class LoginController {

    def authService
    def validationMessagesService
    def loginAttemptService
    def auditService

    def index() {
        if (session.user) {
            redirectBasedOnRole()
        }
    }

    def attempt() {
        String emailKey = params.email?.toString()?.toLowerCase()
        String ipKey = "ip:${request.remoteAddr}"
        if (loginAttemptService.isBlocked(emailKey) || loginAttemptService.isBlocked(ipKey)) {
            flash.error = "Trop de tentatives echouees. Compte temporairement verrouille, reessayez plus tard."
            render(view: "/login/index")
            return
        }
        def user = authService.authenticate(params.email, params.password)
        if (user) {
            loginAttemptService.registerSuccess(emailKey)
            loginAttemptService.registerSuccess(ipKey)
            auditService.log(user, "LOGIN", "Personnel", user.id)
            session.user = user
            redirectBasedOnRole()
        } else {
            loginAttemptService.registerFailure(emailKey)
            loginAttemptService.registerFailure(ipKey)
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
        String ipKey = "register:${request.remoteAddr}"
        if (loginAttemptService.registrationsInWindow(ipKey) >= loginAttemptService.maxRegistrations) {
            flash.error = "Trop de comptes crees depuis cette adresse. Reessayez plus tard."
            render(view: "/register/index", model: [personnel: new Personnel(params)])
            return
        }
        def personnel = new Personnel(
            nom: params.nom,
            prenom: params.prenom,
            email: params.email,
            motDePasse: params.motDePasse,
            role: RolePersonnel.USER
        )
        Personnel.withTransaction { status ->
            if (personnel.save(flush: true)) {
                loginAttemptService.registerRegistration(ipKey)
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
