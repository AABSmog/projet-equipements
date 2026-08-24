package proj.equipment.controller

import groovy.transform.CompileStatic
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

@CompileStatic
class HttpUtil {

    static HttpResponse<Map<String, Object>> erreur(HttpStatus status, String message) {
        HttpResponse.status(status).body([error: message]) as HttpResponse<Map<String, Object>>
    }

    static HttpResponse<Map<String, Object>> ok(Object data) {
        HttpResponse.ok(data) as HttpResponse<Map<String, Object>>
    }
}