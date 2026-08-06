package proj.equipment

class UrlMappings {

    static mappings = {
        "/login"(controller: "login", action: "index")
        "/login/attempt"(controller: "login", action: "attempt")
        "/logout"(controller: "login", action: "logout")
        "/register"(controller: "login", action: "register")
        "/register/save"(controller: "login", action: "saveRegistration")

        "/admin"(controller: "admin", action: "index", namespace: "admin")
        "/admin/$controller/$action?/$id?(.$format)?"(namespace: "admin")

        "/app"(controller: "equipement", action: "list", namespace: "app")
        "/app/$controller/$action?/$id?(.$format)?"(namespace: "app")

        "/"(controller: "login", action: "index")
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}