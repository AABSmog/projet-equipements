package proj.equipment.filter

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Singleton
import org.reactivestreams.Publisher

/**
 * Applique des en-tetes de securite sur toutes les reponses HTML.
 */
@CompileStatic
@Singleton
@Filter('/**')
class SecurityHeadersFilter implements HttpServerFilter {

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        return reactor.core.publisher.Flux.from(chain.proceed(request)).doOnNext { response ->
            response.getHeaders()
                    .add('X-Frame-Options', 'DENY')
                    .add('X-Content-Type-Options', 'nosniff')
                    .add('Referrer-Policy', 'no-referrer')
                    .add('X-XSS-Protection', '1; mode=block')
                    .add('Permissions-Policy', 'geolocation=(), microphone=(), camera=()')
                    .add('Content-Security-Policy',
                            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                                    "img-src 'self' data:; font-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self'")
            if (request.isSecure()) {
                response.getHeaders().add('Strict-Transport-Security', 'max-age=31536000; includeSubDomains')
            }
        }
    }
}