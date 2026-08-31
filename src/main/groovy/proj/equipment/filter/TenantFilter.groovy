package proj.equipment.filter

import groovy.transform.CompileStatic
import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.Optional
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import proj.equipment.domain.Etablissement
import proj.equipment.service.EtablissementService
import proj.equipment.service.SessionManager
import proj.equipment.service.TenantContext

@CompileStatic
@Singleton
@Filter('/api/**')
class TenantFilter implements HttpServerFilter {

    @Inject SessionManager sessionManager
    @Inject EtablissementService etablissementService
    @Inject TenantContext tenantContext

    @Override
    Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        Optional<Map<String, Object>> optSession = sessionManager.get(request)
        if (optSession.present) {
            Map<String, Object> user = optSession.get().get(SessionManager.ATTR_USER) as Map
            def etabData = user?.get('etablissement') as Map
            if (etabData?.get('id')) {
                Etablissement etab = etablissementService.findById((etabData.get('id') as Long))
                if (etab) tenantContext.set(etab)
            }
        }
        Flux.from(chain.proceed(request))
                .doOnTerminate { tenantContext.clear() }
    }
}