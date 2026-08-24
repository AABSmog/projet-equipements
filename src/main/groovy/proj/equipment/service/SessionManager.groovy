package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.cookie.Cookie
import jakarta.inject.Singleton

/**
 * Gestion de session applicative en memoire (equivalent d'un HttpSession).
 * Un identifiant opaque est envoye dans un cookie securise ; les donnees
 * (utilisateur, jeton CSRF) sont conservees cote serveur.
 */
@Singleton
@CompileStatic
class SessionManager {

    static final String COOKIE = 'equipments-id'
    static final String ATTR_USER = 'user'
    static final String ATTR_TOKEN = 'csrfToken'

    @Value('${SESSION_MAX_AGE:1800}')
    long maxAgeSeconds

    @Value('${SESSION_COOKIE_SECURE:false}')
    boolean secureCookie

    private final Map<String, SessionData> sessions = Collections.synchronizedMap(new LinkedHashMap<>())

    static class SessionData {
        final String id
        long lastAccess
        final Map<String, Object> attributes = new LinkedHashMap<>()
        SessionData(String id) {
            this.id = id
            touch()
        }
        void touch() { lastAccess = System.currentTimeMillis() }
    }

    Optional<Map<String, Object>> get(HttpRequest<?> request) {
        String id = cookieValue(request)
        if (!id) return Optional.empty()
        purgeExpired()
        SessionData data = sessions.get(id)
        if (data) {
            data.touch()
            return Optional.of(data.attributes)
        }
        Optional.empty()
    }

    Map<String, Object> start(HttpRequest<?> request, MutableHttpResponse<?> response) {
        Optional<Map<String, Object>> existing = get(request)
        if (existing.present) {
            return existing.get()
        }
        purgeExpired()
        String id = UUID.randomUUID().toString()
        SessionData data = new SessionData(id)
        sessions.put(id, data)
        response.header(HttpHeaders.SET_COOKIE, cookie(id))
        data.attributes
    }

    void invalidate(HttpRequest<?> request, MutableHttpResponse<?> response) {
        String id = cookieValue(request)
        if (id) {
            sessions.remove(id)
            response.header(HttpHeaders.SET_COOKIE, expiredCookie())
        }
    }

    void touch(HttpRequest<?> request) {
        String id = cookieValue(request)
        if (id) {
            sessions.get(id)?.touch()
        }
    }

    private String cookie(String id) {
        StringBuilder sb = new StringBuilder()
        sb.append(COOKIE).append('=').append(id)
        sb.append('; Path=/')
        sb.append('; Max-Age=').append(maxAgeSeconds)
        sb.append('; HttpOnly')
        sb.append('; SameSite=Lax')
        if (secureCookie) {
            sb.append('; Secure')
        }
        sb.toString()
    }

    private String expiredCookie() {
        "${COOKIE}=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
    }

    private static String cookieValue(HttpRequest<?> request) {
        Cookie cookie = request.cookies?.get(COOKIE)
        cookie?.value
    }

    private void purgeExpired() {
        long cutoff = System.currentTimeMillis() - maxAgeSeconds * 1000
        sessions.entrySet().removeIf { e -> e.value.lastAccess < cutoff }
        if (sessions.size() > 1000) {
            // au-dela de 1000 sessions actives, nettoyer les plus anciennes
            def sorted = sessions.values().sort { a, b -> a.lastAccess <=> b.lastAccess }
            int surplus = sessions.size() - 500
            sorted.take(surplus).each { sessions.remove(it.id) }
        }
    }
}