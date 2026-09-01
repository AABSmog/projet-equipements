package proj.equipment.filter

import groovy.transform.CompileStatic
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Singleton
import proj.equipment.service.SessionManager
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono

/**
 * Protection CSRF : jeton stocke en session, controle sur chaque requete
 * POST / PUT / PATCH / DELETE sous /api/** via l'en-tete X-CSRF-Token.
 */
@CompileStatic
@Singleton
@Filter('/api/**')
class CsrfFilter implements HttpServerFilter {

    private static final Set<HttpMethod> MUTATIONS = [HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE] as Set

    private final SessionManager sessionManager

    CsrfFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager
    }

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (!MUTATIONS.contains(request.method)) {
            return chain.proceed(request)
        }
        // Wizard public + auth (login/register) accessibles sans CSRF prealable
        if (request.path == '/api/etablissements/wizard' || request.path == '/api/auth/login' || request.path == '/api/auth/register') {
            return chain.proceed(request)
        }
        Optional<Map<String, Object>> optSession = sessionManager.get(request)
        if (!optSession.present) {
            return Mono.just(HttpResponse.status(HttpStatus.BAD_REQUEST, 'Requete rejetee : jeton de securite invalide.')
                    .body([error: 'Requete rejetee : jeton de securite invalide.']))
        }
        Map<String, Object> session = optSession.get()
        String token = session.get(SessionManager.ATTR_TOKEN) as String
        if (!token) {
            return Mono.just(HttpResponse.status(HttpStatus.BAD_REQUEST, 'Requete rejetee : jeton de securite invalide.')
                    .body([error: 'Requete rejetee : jeton de securite invalide.']))
        }
        String header = request.headers.get('X-CSRF-Token')
        if (!header) {
            return Mono.just(HttpResponse.status(HttpStatus.BAD_REQUEST, 'Requete rejetee : jeton de securite invalide.')
                    .body([error: 'Requete rejetee : jeton de securite invalide.']))
        }
        if (header != token) {
            return Mono.just(HttpResponse.status(HttpStatus.BAD_REQUEST, 'Requete rejetee : jeton de securite invalide.')
                    .body([error: 'Requete rejetee : jeton de securite invalide.']))
        }
        chain.proceed(request)
    }
}