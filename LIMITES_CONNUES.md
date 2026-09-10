# Limites connues — Micronaut v3

## Anomalies traitées v1 (contraintes)
| # | Sujet | Correction |
|---|-------|------------|
| 1 | Suppression personnel avec historique | Bloquée si affectations/signalements (`AdminPersonnelController:93`) |
| 2 | État libre | `validateEquipement` : AFFECTÉ exige active, etc. |
| 3 | Mdp faible | 8 + complexité (`validatePersonnel`) |
| 4 | Types doublons casse | `lower(nom)` |
| 5 | dateRetour antérieure | `validateAffectation` |
| 6 | Signalement sans type | `validateSignalement` |
| 7 | Attribution orpheline | Transaction + validation préalable |
| 8 | Équipement sans type | `validateEquipement` |
| 9 | Mdp en dur | `${EQUIPMENTS_DB_PASSWORD}` |

## v2 (sécurité, perf)
| 10 | Brute-force | `LoginAttemptService` 5/15min |
| 11 | CSRF | `CsrfFilter` `X-CSRF-Token`, exempt `wizard/login/register` |
| 12 | Audit | `AuditLog` + `/api/admin/audit` |
| 13 | Pagination | `max/offset` max 100 |
| 14 | Headers | `SecurityHeadersFilter` CSP etc. |
| 15 | Session | HttpOnly SameSite Lax 30m |
| 16 | N+1 | `JOIN FETCH attribuePar` |
| 17 | Index | JPA indexes |

## v3 (terminologie, équipements, jeton) — 2026-09-01
| 18 | Terminologie Etablissement → Entreprise | UI renommée (`index.html`, wizards, `layout.js`) + messages `Entreprise introuvable` |
| 19 | Termes techniques (slug/Base/equipments_) | `Code (auto)`, `Domaine`, `Votre espace sera isolé`, `Session expirée` au lieu de `jeton` |
| 20 | Equipements manquants par entreprise | `BootstrapSeed.assurerEquipementsPourTous()` 6/entreprise si <5 |
| 21 | Jeton invalide après login (rotation non synchronisée) | `AuthController` renvoie `csrfToken`, `api.js` le met à jour |
| 22 | Login sans session → jeton invalide | `login.js` `await Api.initSession()` + `CsrfFilter` exempte `login/register` |
| 23 | `GString` message sérialisé en objet | `.toString()` dans `EtablissementService` |
| 24 | Mdp uniformes peu réalistes | `USERS_MOTS_DE_PASSE.txt` variés + `BootstrapSeed` map réaliste |
| 25 | Equipements démo réduits | 4 → 2 entreprises (somdop, coulibaly) |

## Limites restantes
- **Sécurité** : mdp oublié absent, session mémoire non distribuée, brute-force en mémoire, pas de captcha.
- **Données** : suppression équipement détache `infoEquipement`, SN réutilisable, `validate` vs `update` (Flyway recommandé), pas de backup auto, concurrence `@Version` dernier écrase.
- **Fonctionnel** : `EN_PANNE/REPARE` non automatisés, pagination max 100.
- **Tests** : 45 Spock (8 suites) couvrent domaine + services + intégration ; écrans via recette manuelle (20 scénarios v3, 42 v2).

## Recette
- v3 (01/09/2026) : 20 scénarios, 20 OK (dont cas limites matériel déjà attribué, restitution sans attribution, saisies invalides) — `cahiers-de-recette/recette-micronaut-v3.md`
- v2 (11/08/2026) : 42 scénarios, 42 OK

## Reseed
`BootstrapSeed` si `type_equipement` vide ; mdp via `USERS_MOTS_DE_PASSE.txt` (uniformisation au boot).
