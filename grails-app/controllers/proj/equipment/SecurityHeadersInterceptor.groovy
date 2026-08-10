package proj.equipment

/**
 * Applique des en-tetes de securite sur toutes les reponses HTML.
 * Aucun impact sur les requetes elles-memes.
 */
class SecurityHeadersInterceptor {

    SecurityHeadersInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("X-Frame-Options", "DENY")
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("Referrer-Policy", "no-referrer")
        response.setHeader("X-XSS-Protection", "1; mode=block")
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()")
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:; font-src 'self' data:; object-src 'none'; base-uri 'self'; form-action 'self'")
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        }
        true
    }
}