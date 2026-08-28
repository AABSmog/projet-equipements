package proj.equipment.service

import jakarta.inject.Singleton
import proj.equipment.domain.Etablissement

/**
 * Contexte du tenant courant (etablissement).
 * Alimenté par le filtre d'authentification à partir de l'utilisateur connecté.
 * En mode single-DB, sert à filtrer toutes les requêtes par etablissement_id.
 * En mode multi-DB, servira à router vers la DataSource dédiée.
 */
@Singleton
class TenantContext {

    private final ThreadLocal<Etablissement> current = new ThreadLocal<>()

    void set(Etablissement e) { current.set(e) }
    Etablissement get() { current.get() }
    void clear() { current.remove() }

    Long currentId() { get()?.id }
    String currentSlug() { get()?.slug }
}
