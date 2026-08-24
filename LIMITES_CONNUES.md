# Limites connues — Version Micronaut

Document recensant les limites et anomalies non traitées de la version **Micronaut** de la gestion d'équipements.
Complète les cahiers de recette (`cahiers-de-recette/`). Mise à jour : v2 (2026-08-11).

## Anomalies traitées dans cette version (renforcement)

| # | Sujet | Correction |
|---|-------|------------|
| 1 | Suppression d'un personnel avec historique effaçait l'historique | Blocage : `AdminPersonnelController` refuse si `affectations` ou `signalements` existants ; auto-suppression interdite |
| 2 | État modifiable librement (AFFECTÉ sans affectation) | `ValidationMessagesService.validateEquipement` : AFFECTÉ exige affectation active, DISPONIBLE/HORS_SERVICE interdits si affectation active, création toujours DISPONIBLE |
| 3 | Mot de passe sans longueur minimale | Politique 8car + complexité minuscule/majuscule/chiffre (`validatePersonnel`) |
| 4 | Types duplicables avec casse différente | `validateTypeEquipement` + requête `lower(nom)` ; `findByNomIlike` dans BootstrapSeed |
| 5 | `dateRetour` antérieure à `dateAffectation` acceptée | `validateAffectation` : `dateRetour >= dateAffectation` |
| 6 | Signalement sans type | `validateSignalement` : `type` obligatoire |
| 7 | `attribuer` : affectation orpheline si `equipement.save` échouait | Validation préalable + `em.persist`/`merge` dans même transaction, rollback si erreur |
| 8 | Équipement sans type | `validateEquipement` : `type` obligatoire |
| 9 | Mot de passe en dur dans `application.yml` | `datasources.default.password: ${EQUIPMENTS_DB_PASSWORD}` (plus de défaut) ; `url` via `${EQUIPMENTS_DB_URL:`...`}` |

## Anomalies traitées dans la v2 (sécurité, performances, audabilité)

| # | Sujet | Correction (v2) |
|---|-------|-----------------|
| 10 | Brute force connexion | `LoginAttemptService` : 5 échecs / 15 min → blocage 5 min (email+IP), quota register 3 / fenêtre |
| 11 | Inscription ouverte | Quota `register` par IP |
| 12 | CSRF absent | `CsrfFilter` : `X-CSRF-Token` validé sur POST/PUT/PATCH/DELETE |
| 13 | Pas d'audit | `AuditLog` + `AuditService.log/recent` + `GET /api/admin/audit` + `admin/audit.html` |
| 14 | Pas de pagination | `CatalogService.liste*` paginé `max/offset` (max 100), filtres conservés (`q`, `typeId`, `etat`) |
| 15 | En-têtes HTTP faibles | `SecurityHeadersFilter` : CSP, X-Frame DENY, nosniff, Referrer-Policy, HSTS |
| 16 | Cookie session | `micronaut.session.http.cookie` HttpOnly + SameSite=Lax, 30m |
| 17 | N+1 sur listes | `JOIN FETCH` via `CatalogService` + `EntityGraph` |
| 18 | Indexs absents | Index JPA sur `equipement(type,etat)`, `affectation(equipement,personnel,date)`, `audit_log`, `personnel(email)` |

## Limites restantes (non traitées)

### Sécurité
- **Mot de passe oublié** : pas de procédure ; seul l'admin réinitialise.
- **Session en mémoire** : `SessionManager` en mémoire, non distribué ; pas de JWT/OAuth.
- **Compteur brute-force en mémoire** : perdu au redémarrage, non partagé entre instances.
- **Captcha / email verification** : absents (quota compense partiellement).

### Fiabilité / données
- **Suppression équipement** : détache affectations/signalements (`infoEquipement`) puis supprime ; SN réutilisable.
- **Schéma** : `hibernate.hbm2ddl.auto=validate` (prod) vs `update` en dev ; migrer vers Flyway/Liquibase recommandé.
- **Pas de backup automatisé** PostgreSQL.
- **Concurrence optimiste** : `@Version` présent mais collisions non gérées UI (dernier écrase).
- **Assets** : `tailwind.css` précompilé, pas de cache-manifest.

### Fonctionnel
- **Signalement → état non automatisé** : `EN_PANNE`/`REPARE` manuels, non tracés.
- **États EN_PANNE/REPARE non bornés** : seule cohérence AFFECTÉ/DISPONIBLE/HORS_SERVICE vérifiée.

### Messages / Tests
- **Messages FR partiels** : `ValidationMessagesService` couvre principaux cas, reste fallback générique.
- **Tests** : 45 Spock (43 unit. + 2 intégration) verts, écrans validés via recette manuelle (`recette-v2.md`).

## Recette
- **v2 (11/08/2026)** : 42 scénarios, 42 OK, 0 KO — portage Grails → Micronaut à l'identique (REST + JS). Voir `cahiers-de-recette/recette-v2.md`.

## Note
Reseed `BootstrapSeed` si `type_equipement` vide ; mots de passe via `DEMO_ADMIN_PASSWORD` / `DEMO_USER_PASSWORD`.
