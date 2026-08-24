package proj.equipment.filter

import groovy.transform.CompileStatic
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
 * Controle d'acces : /api/admin/** reserve a l'ADMIN, /api/app/** aux
 * utilisateurs connectes. Reponse JSON en cas de refus.
 */
@CompileStatic
@Singleton
@Filter(['/api/admin/**', '/api/app/**'])
class AuthFilter implements HttpServerFilter {

    private final SessionManager sessionManager

    AuthFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager
    }

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        Optional<Map<String, Object>> optSession = sessionManager.get(request)
        if (!optSession.present) {
            return unauthorized()
        }
        Map<String, Object> user = optSession.get().get(SessionManager.ATTR_USER) as Map
        if (!user) {
            return unauthorized()
        }
        String uri = request.path
        if (uri.startsWith('/api/admin') && user.get('role') != 'ADMIN') {
            return Mono.just(HttpResponse.status(HttpStatus.FORBIDDEN, 'Acces interdit')
                    .body([error: 'Acces interdit']))
        }
        chain.proceed(request)
    }

    private static Publisher<MutableHttpResponse<?>> unauthorized() {
        Mono.just(HttpResponse.status(HttpStatus.UNAUTHORIZED, 'Authentification requise')
                .body([error: 'Authentification requise']))
    }
}