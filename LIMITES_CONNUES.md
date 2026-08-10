# Limites connues

Document recensant les limites, points faibles et anomalies non traitées de l'application
de gestion d'équipements. Il complète les cahiers de recette (`cahiers-de-recette/`).
Mise à jour : v2 (2026-08-10).

## Anomalies traitées dans cette version (renforcement)

| # | Sujet | Correction |
|---|-------|------------|
| 1 | Suppression d'un personnel : la cascade `belongsTo` effaçait silencieusement tout l'historique (affectations + signalements) | Blocage : impossible de supprimer un personnel ayant un historique ; impossible de supprimer son propre compte |
| 2 | L'état d'un équipement était modifiable librement → incohérences possibles (AFFECTÉ sans affectation, DISPONIBLE avec affectation active) | Validateur de cohérence : AFFECTÉ exige une affectation active ; DISPONIBLE / HORS_SERVICE interdits tant qu'une affectation active existe ; un nouvel équipement est toujours créé DISPONIBLE |
| 3 | Mot de passe sans longueur minimale | Politique renforcée : 8 caractères min, au moins une minuscule, une majuscule et un chiffre (création, modification, inscription publique) |
| 4 | Types d'équipement dupliquables avec casse différente (ex. « Scanner » / « scanner ») | Validateur d'unicité insensible à la casse (en plus du dédoublonnage par `findByNomIlike` dans le contrôleur) |
| 5 | Date de retour antérieure à la date d'affectation acceptée | Validateur `dateRetour >= dateAffectation` |
| 6 | Signalement sans type de problème | `type` obligatoire |
| 7 | `affecter` : si la sauvegarde de l'équipement échouait, l'affectation restait créée (orpheline) | Vérification des sauvegardes + rollback explicite de la transaction |
| 8 | Équipement sans type | `type` obligatoire (associé) |

## Anomalies traitées dans la v2 (sécurité, performances, audabilité)

| # | Sujet | Correction (v2) |
|---|-------|-----------------|
| 9 | Brute force sur la connexion : aucune limitation de tentatives | `LoginAttemptService` : blocage par email **et** IP après `maxAttempts` (5) échecs sur une fenêtre de 15 min, verrouillage 5 min, remise à zéro après réussite |
| 10 | Inscription publique ouverte | Quota de créations de comptes (3 par fenêtre, par IP/email), géré par `LoginAttemptService` |
| 11 | Politique de mot de passe faible | Complexité ajoutée (voir #3) ; messages français via `ValidationMessagesService` |
| 12 | Protection CSRF absente | `CsrfInterceptor` : jeton de session `_csrf` validé sur toutes les requêtes POST/PUT/PATCH/DELETE (formulaires + actions d'état converties en POST avec confirmation) |
| 13 | Aucun journal d'audit | Domain `AuditLog` + `AuditService` (création, modification, suppression, attribution, retour, déclassement, signalement, connexion) ; écran `/admin/audit` |
| 14 | Pas de pagination sur les listes | Liste paginée (`max`/`offset`, limite 100) sur équipements, personnels, affectations, historique, signalements, types et écrans `/app` ; les filtres de recherche sont conservés |
| 15 | En-têtes HTTP par défaut de Spring Boot insuffisants | `SecurityHeadersInterceptor` : CSP, X-Frame-Options DENY, nosniff, Referrer-Policy, X-XSS-Protection, Permissions-Policy, HSTS en HTTPS |
| 16 | Cookies de session sans attributs | Cookie `HttpOnly` + `SameSite=Lax`, durée de session 30 min configurée |
| 17 | Erreurs type « N+1 » sur les listes (affectations, signalements) | `fetchMode: JOIN` et requêtes groupées (`Affectation` en une requête pour la liste des équipements) |
| 18 | Indexs absents sur les colonnes de recherche/filtrage | Index créés : équipement (type, état), affectation (équipement, personnel, date, retour), signalement, audit (date, cible), personnel (email, rôle) |

## Anomalies et limites restantes (non traitées)

### Sécurité
- **Réinitialisation de mot de passe** : aucune procédure « mot de passe oublié » ; seul l'admin peut changer le mot de passe d'un compte.
- **Authentification par session HTTP** : sessions en mémoire du conteneur, non distribuées ; aucune API/token (JWT, OAuth). En multi-instances, prévoir un store de sessions partagé.
- **Échecs de connexion en mémoire** : le compteur `LoginAttemptService` est en mémoire par instance ; redondant entre plusieurs instances, et réinitialisé au redémarrage.
- **Captcha** : pas de captcha sur l'inscription ni la connexion (le quota IP compense partiellement).
- **Confirmation d'adresse email** : pas de vérification d'email à l'inscription.

### Fiabilité / données
- **Suppression d'un équipement** : l'action `delete` (non exposée dans l'UI) détache les affectations et signalements puis supprime l'équipement ; le numéro de série peut alors être réutilisé. La suppression d'un type d'équipement est désactivée.
- **Schéma géré par Hibernate** (`dbCreate: update`) : pratique en dev/demo ; pour la production, migrer vers Flyway/Liquibase puis passer `dbCreate: validate` (recommandation documentée dans le `README`).
- **Pas de sauvegarde automatisée** de la base PostgreSQL (aucune procédure de backup/restore).
- **Conflits de modification concurrente** : les versions optimistes GORM existent mais les collisions ne sont pas gérées explicitement (l'utilisateur le plus récent écrase).
- **Compression des assets** : Tailwind compilé au build ; pas de cache-manifest/service worker (mode hors-ligne non couvert).

### Fonctionnel
- **Signalement → état non automatisé** : un signalement (panne/réparation) ne modifie pas automatiquement l'état de l'équipement ; le passage à `EN_PANNE`/`REPARE` reste manuel (via l'édition) et non tracé.
- **États `EN_PANNE` / `REPARE` non bornés** : la cohérence est vérifiée uniquement pour `AFFECTÉ`, `DISPONIBLE` et `HORS_SERVICE` ; un équipement peut être passé en panne sans signalement préalable, ou réparé sans historique de panne.
- **Édition de l'état `EN_PANNE`/`REPARE` libre** dans le formulaire de modification (aucune règle métier associée).

### Messages et internationalisation
- **Couverte partiellement** : `ValidationMessagesService` traduit les principaux codes ; certains retombent encore sur le message générique anglais ou « Une ou plusieurs saisies sont invalides. ».

### Tests
- **Suite automatisée partielle** : 43 tests unitaires + 2 tests d'intégration (Spock), mais les écrans à ancrage visuel (GSP) restent validés manuellement via les cahiers de recette (`recette-v1.md`).

## Note
La base de données doit être conservée dans l'état « seed » décrit en fin de `cahiers-de-recette/recette-v1.md`
avant chaque exécution de recette.
