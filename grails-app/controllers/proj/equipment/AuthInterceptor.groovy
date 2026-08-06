package proj.equipment

class AuthInterceptor {

    AuthInterceptor() {
        match(uri: "/admin/**")
        match(uri: "/app/**")
    }

    boolean before() {
        if (!session.user) {
            redirect(controller: "login", action: "index")
            return false
        }
        if (request.forwardURI?.startsWith("/admin") && session.user.role != RolePersonnel.ADMIN) {
            redirect(uri: "/app")
            return false
        }
        true
    }

    boolean after() { true }
}
