package proj.equipment.service

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import proj.equipment.domain.Personnel

@Singleton
@CompileStatic
class CurrentUserService {

    @Inject SessionManager sessionManager
    @Inject EntityLookupService lookup

    Personnel get(HttpRequest<?> request) {
        Optional<Map<String, Object>> opt = sessionManager.get(request)
        if (!opt.present) return null
        Object u = opt.get().get(SessionManager.ATTR_USER)
        if (!(u instanceof Map)) return null
        Object idObj = ((Map) u).get('id')
        if (!(idObj instanceof Number)) return null
        lookup.userById((Long) idObj)
    }
}