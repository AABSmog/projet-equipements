package proj.equipment

import groovy.util.logging.Slf4j

/**
 * Protection CSRF : genere un jeton de session et le controle sur chaque
 * requete POST / PUT / PATCH / DELETE. Tous les formulaires doivent
 * transmettre ce jeton via un champ cache nomme "_csrf".
 *
 * Les requetes non authentifiees arrivent sur la page de connexion (GET) qui
 * cree le jeton ; le POST de connexion / inscription (publique) est egalement
 * controle.
 */
@Slf4j
class CsrfInterceptor {

    CsrfInterceptor() {
        matchAll()
    }

    boolean before() {
        if (!session.csrfToken) {
            session.csrfToken = UUID.randomUUID().toString()
        }
        if (request.method in ['POST', 'PUT', 'PATCH', 'DELETE']) {
            String uri = request.forwardURI
            boolean formulairePublic = uri == '/login/attempt' || uri == '/register/save'
            if (session.user || formulairePublic) {
                if (params._csrf != session.csrfToken) {
                    log.warn("Jeton CSRF invalide pour {} {}", request.method, uri)
                    response.sendError(400, "Requete rejetee : jeton de securite invalide.")
                    return false
                }
            }
        }
        true
    }
}