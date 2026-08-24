package proj.equipment.service

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton

/**
 * Protection contre les attaques par force brute sur la connexion
 * et contre la creation abusive de comptes (stockage en memoire).
 */
@Slf4j
@Singleton
class LoginAttemptService {

    @Value('${app.security.login.maxAttempts:5}')
    int maxAttempts

    @Value('${app.security.login.windowSeconds:900}')
    long windowSeconds

    @Value('${app.security.login.lockDurationSeconds:300}')
    long lockDurationSeconds

    @Value('${app.security.register.maxPerWindow:3}')
    int maxRegistrations

    @Value('${app.security.register.windowSeconds:3600}')
    long registrationWindowSeconds

    private final Map<String, List<Date>> failures = Collections.synchronizedMap(new LinkedHashMap<>())
    private final Map<String, Date> lockouts = Collections.synchronizedMap(new LinkedHashMap<>())
    private final Map<String, List<Date>> registrations = Collections.synchronizedMap(new LinkedHashMap<>())

    boolean isBlocked(String key) {
        Date now = new Date()
        if (!key) {
            return false
        }
        Date lockUntil = lockouts.get(key)
        if (lockUntil) {
            if (lockUntil.after(now)) {
                return true
            }
            synchronized (lockouts) { lockouts.remove(key) }
        }
        List<Date> list = failures.get(key)
        if (list) {
            Date cutoff = new Date(now.time - windowSeconds * 1000)
            synchronized (failures) {
                list.removeAll { Date it -> it.before(cutoff) }
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
            List<Date> list = failures.get(key) ?: []
            list = new ArrayList<>(list)
            list.add(now)
            list.removeAll { Date it -> it.before(cutoff) }
            failures.put(key, list)
            if (list.size() >= maxAttempts) {
                lockouts.put(key, new Date(now.time + lockDurationSeconds * 1000))
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
            List<Date> list = registrations.get(key) ?: []
            list = new ArrayList<>(list)
            list.removeAll { Date it -> it.before(cutoff) }
            if (list.isEmpty()) {
                registrations.remove(key)
                return 0
            }
            registrations.put(key, list)
            return list.size()
        }
    }

    void registerRegistration(String key) {
        if (!key) return
        synchronized (registrations) {
            List<Date> list = registrations.get(key) ?: []
            list = new ArrayList<>(list)
            list.add(new Date())
            registrations.put(key, list)
        }
    }
}