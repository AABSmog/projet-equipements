package proj.equipment

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value

/**
 * Protection contre les attaques par force brute sur la connexion
 * et contre la creation abusive de comptes.
 *
 * Comptabilise les echecs de connexion par adresse e-mail et par adresse IP :
 * au-dela du seuil, le compte / l'adresse est temporairement verrouille.
 * Le stockage est en memoire (suffisant pour une application monoprocess).
 */
@Slf4j
class LoginAttemptService {

    @Value('${security.login.maxAttempts:5}')
    int maxAttempts
    @Value('${security.login.windowSeconds:900}')
    long windowSeconds
    @Value('${security.login.lockDurationSeconds:300}')
    long lockDurationSeconds
    @Value('${security.register.maxPerWindow:3}')
    int maxRegistrations
    @Value('${security.register.windowSeconds:3600}')
    long registrationWindowSeconds

    private final Map<String, List<Date>> failures = Collections.synchronizedMap(new LinkedHashMap<>())
    private final Map<String, Date> lockouts = Collections.synchronizedMap(new LinkedHashMap<>())
    private final Map<String, List<Date>> registrations = Collections.synchronizedMap(new LinkedHashMap<>())

    boolean isBlocked(String key) {
        Date now = new Date()
        if (!key) {
            return false
        }
        Date lockUntil = lockouts[key]
        if (lockUntil) {
            if (lockUntil.after(now)) {
                return true
            }
            synchronized (lockouts) { lockouts.remove(key) }
        }
        List<Date> list = failures[key]
        if (list) {
            Date cutoff = new Date(now.time - windowSeconds * 1000)
            synchronized (failures) {
                list.removeAll { it.before(cutoff) }
                if (list.isEmpty()) failures.remove(key)
            }
            return list.size() >= maxAttempts
        }
        false
    }

    void registerFailure(String key) {
        if (!key) return
        Date now = new Date()
        Date cutoff = new Date(now.time - windowSeconds * 1000)
        synchronized (failures) {
            List<Date> list = failures[key] ?: []
            list << now
            list.removeAll { it.before(cutoff) }
            failures[key] = list
            if (list.size() >= maxAttempts) {
                lockouts[key] = new Date(now.time + lockDurationSeconds * 1000)
                failures.remove(key)
                log.warn("Compte / adresse verrouille apres {} echecs de connexion : {}", maxAttempts, key)
            }
        }
    }

    void registerSuccess(String key) {
        if (!key) return
        synchronized (failures) { failures.remove(key) }
        synchronized (lockouts) { lockouts.remove(key) }
    }

    int registrationsInWindow(String key) {
        if (!key) return 0
        Date now = new Date()
        Date cutoff = new Date(now.time - registrationWindowSeconds * 1000)
        synchronized (registrations) {
            List<Date> list = registrations[key] ?: []
            list.removeAll { it.before(cutoff) }
            if (list.isEmpty()) {
                registrations.remove(key)
                return 0
            }
            return list.size()
        }
    }

    void registerRegistration(String key) {
        if (!key) return
        synchronized (registrations) {
            List<Date> list = registrations[key] ?: []
            list << new Date()
            registrations[key] = list
        }
    }
}