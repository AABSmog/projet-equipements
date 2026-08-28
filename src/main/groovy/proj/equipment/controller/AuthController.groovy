package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import jakarta.inject.Inject
import proj.equipment.domain.Personnel
import proj.equipment.dto.ApiModels
import proj.equipment.service.*
import proj.equipment.service.SessionManager

@CompileStatic
@Controller('/api/auth')
class AuthController {

    @Inject AuthService authService
    @Inject LoginAttemptService loginAttemptService
    @Inject AuditService auditService
    @Inject AccountService accountService
    @Inject SessionManager sessionManager

    @Get('/session')
    HttpResponse<Map<String, Object>> session(HttpRequest<?> request) {
        MutableHttpResponse<Map<String, Object>> response = HttpResponse.status(HttpStatus.OK)
        Map<String, Object> attrs = sessionManager.start(request, response)
        if (!attrs.containsKey(SessionManager.ATTR_TOKEN)) {
            attrs.put(SessionManager.ATTR_TOKEN, UUID.randomUUID().toString())
        }
        Map<String, Object> user = attrs.get(SessionManager.ATTR_USER) as Map<String, Object>
        Map<String, Object> body = [
                user    : user,
                csrfToken: attrs.get(SessionManager.ATTR_TOKEN)
        ]
        response.body(body)
    }

    @Post('/login')
    HttpResponse<Map<String, Object>> login(HttpRequest<?> request, @Body Map<String, Object> body) {
        String email = body.email as String
        String password = body.password as String
        String emailKey = email?.toString()?.toLowerCase()
        String ipKey = "ip:${request.remoteAddress?.address?.hostAddress}"

        if (loginAttemptService.isBlocked(emailKey) || loginAttemptService.isBlocked(ipKey)) {
            return erreur(HttpStatus.BAD_REQUEST, 'Trop de tentatives echouees. Compte temporairement verrouille, reessayez plus tard.')
        }

        Personnel user = authService.authenticate(email, password)
        if (user) {
            loginAttemptService.registerSuccess(emailKey)
            loginAttemptService.registerSuccess(ipKey)
            auditService.log(user, 'LOGIN', 'Personnel', user.id)
            MutableHttpResponse<Map<String, Object>> response = HttpResponse.status(HttpStatus.OK)
            Map<String, Object> attrs = sessionManager.start(request, response)
            attrs.put(SessionManager.ATTR_USER, ApiModels.user(user))
            // Rotation du jeton CSRF au login pour prévenir la fixation
            attrs.put(SessionManager.ATTR_TOKEN, UUID.randomUUID().toString())
            response.body([user: ApiModels.user(user)] as Map<String, Object>)
            return response
        } else {
            loginAttemptService.registerFailure(emailKey)
            loginAttemptService.registerFailure(ipKey)
            return erreur(HttpStatus.UNAUTHORIZED, 'Email ou mot de passe incorrect')
        }
    }

    @Post('/logout')
    HttpResponse<Map<String, Object>> logout(HttpRequest<?> request) {
        MutableHttpResponse<Map<String, Object>> response = HttpResponse.status(HttpStatus.OK)
        sessionManager.invalidate(request, response)
        HttpResponse.ok((Map<String, Object>) [message: 'Deconnecte'])
    }

    @Post('/register')
    HttpResponse<Map<String, Object>> register(HttpRequest<?> request, @Body Map<String, Object> body) {
        String ipKey = "register:${request.remoteAddress?.address?.hostAddress}"
        if (loginAttemptService.registrationsInWindow(ipKey) >= loginAttemptService.maxRegistrations) {
            return erreur(HttpStatus.BAD_REQUEST, 'Trop de comptes crees depuis cette adresse. Reessayez plus tard.')
        }
        Map<String, Object> result = accountService.register(
                body.nom as String,
                body.prenom as String,
                body.email as String,
                body.motDePasse as String
        )
        if (result.success) {
            loginAttemptService.registerRegistration(ipKey)
            return HttpResponse.ok(result)
        }
        erreur(HttpStatus.BAD_REQUEST, result.message as String)
    }

    private static HttpResponse<Map<String, Object>> erreur(HttpStatus status, String message) {
        HttpResponse.status(status).body([error: message]) as HttpResponse<Map<String, Object>>
    }
}