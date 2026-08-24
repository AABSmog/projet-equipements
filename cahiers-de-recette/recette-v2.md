# Cahier de recette — Gestion des équipements (Micronaut)

Version : v2 (après corrections majeures)
Application : Gestion des équipements (Micronaut 5 / PostgreSQL)
Environnement : développement — `http://localhost:8080`
Date d'exécution : 11/08/2026
Base : vidée puis reseed via `BootstrapSeed` (5 types, 9 équipements, 7 personnels, 2 affectations)

Comptes de test (via `POST /api/auth/login` + `X-CSRF-Token`) :
| Rôle  | Email                        | Mot de passe |
|-------|------------------------------|--------------|
| ADMIN | mamadou.diop@example.com     | Admin123     |
| USER  | aissatou.diallo@example.com  | User1234     |

Front : `static/admin/*` + `static/app/*` (API JSON). Logs navigateur : `js/api.js` groupe `[API]`.
Légende : OK = conforme / KO = anomalie.

---

## 1. Authentification & inscription (REST)

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S1 | `POST /api/auth/login` admin valide | 200 + `user.role=ADMIN` + session | JSON OK, cookie HttpOnly Lax | OK |
| S2 | `POST /api/auth/login` user valide | 200 + `role=USER` | JSON OK | OK |
| S3 | Mot de passe incorrect | 401 + message générique | `{"error":"Email ou mot de passe incorrect"}` | OK |
| S4 | 5 échecs consécutifs puis 6e | 400 "Trop de tentatives..." (LoginAttemptService) | bloqué 5 min, `isBlocked` par email+IP | OK |
| S5 | `POST /api/auth/register` email libre | 200, user créé, mdp BCrypt | `{"success":true}`, hash `$2a$` en base | OK |
| S6 | Inscription email déjà utilisé | 400 + message FR | `{"error":"Cet email est deja utilise."}` | OK |
| S7 | Mot de passe faible (sans maj/chiffre) | 400 politique 8car | `{"error":"Le mot de passe doit contenir..."}` | OK |
| S8 | `GET /api/admin/stats` sans session | 401 Authentification requise (AuthFilter) | 401 | OK |
| S9 | USER tente `GET /api/admin/personnels` | 403 (rôle) | 403 | OK |

## 2. Équipement (validation REST)

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S10 | `POST /api/admin/equipements` sans SN | SN auto `SN-XXXXXXXX`, DISPONIBLE | 200, SN généré par `@PrePersist` | OK |
| S11 | Créer avec type inexistant `typeId: autre` via UI JS | type créé puis équipement | 200, 2 inserts dans même requête | OK |
| S12 | `typeId` manquant | 400 "Veuillez selectionner un type" | 400 | OK |
| S13 | SN dupliqué | 400 "deja utilise" (ValidationMessagesService) | 400, insensible casse via `lower()` | OK |
| S14 | Description vide | 400 "obligatoire" | 400 | OK |
| S15 | Forger `etat: HORS_SERVICE` à la création | 400 "doit etre cree a l'etat disponible" | 400 | OK |
| S16 | `PUT /api/admin/equipements/{id}` → AFFECTE sans affectation | 400 "sans affectation active" | 400 | OK |

## 3. Affectation — cas limites métier

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S17 | `POST /api/admin/affectations` eq DISPONIBLE → USER | 200, eq→AFFECTE, `attribuePar`=admin | 200, `attribuePar` persisté | OK |
| S18 | **Matériel déjà attribué** — même eq à autre USER | 400 "n'est pas disponible" | 400, aucune 2e ligne | OK |
| S19 | Même personnel redemande même eq | 400 "deja affecte a ..." | 400 | OK |
| S20 | Eq EN_PANNE | 400 "n'est pas disponible" | 400 | OK |
| S21 | Ids inexistants | 400 "introuvable" | 400 | OK |
| S22 | `POST .../desaffecter` eq AFFECTE | 200, eq→DISPONIBLE, `dateRetour` posée | 200 | OK |
| S23 | **Restitution sans attribution** — `POST /api/admin/affectations/{id}/retour` sans affectation active | 400 "Aucune affectation active" | 400 | OK |
| S24 | `POST /{id}/retour` avec raison | 200, `raisonRetour` enregistrée | 200, `raisonRetour=="Fin de mission"` | OK |
| S25 | Double retour (déjà close) | 400 "deja ete cloturee" | 400 | OK |
| S26 | `POST /api/admin/equipements/{id}/declasser` eq AFFECTE | 200, eq→HORS_SERVICE, `infoEquipement` declasse | 200 | OK |
| S27 | Declasser DISPONIBLE | 200, HORS_SERVICE | 200 | OK |
| S28 | `dateRetour` antérieure à `dateAffectation` (manip directe) | 400 "anterieure" | 400 via `validateAffectation` | OK |

## 4. Signalement

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S29 | USER signale son équipement (`POST /api/app/signalements`) | 200, signalement lié | 200 | OK |
| S30 | Signaler eq non affecté à soi | 400/403 | 400 "Non autorisé" | OK |
| S31 | Description vide | 400 "obligatoire" | 400 | OK |
| S32 | Type null | 400 "obligatoire" | 400 | OK |

## 5. Personnels et types

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S33 | Email invalide / dupliqué (casse) | 400 | 400, `lower(email)` unique | OK |
| S34 | `DELETE /api/admin/personnels/{id}` avec historique | 400 "possede un historique" | 400 | OK |
| S35 | Type dupliqué `Scanner/scanner` | 400 "existe deja" | 400, `lower(nom)` | OK |

## 6. Historique, audit, sécurité, pagination

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S36 | `GET /api/admin/affectations/historique?q=&typeId=&max=10` | JSON paginé `items/total` + `attribuePar` renseigné | 200, pagination `max=100` | OK |
| S37 | Écran `admin/affectation-historique.html` | colonnes Equipement/N°Série/Attribue à/Par/Date affect./Date retour/Raison/Statut/Actions | rendu correct, fetch JOIN sans N+1 | OK |
| S38 | Recherche historique par N° série / admin | filtre LIKE | 200, résultats filtrés | OK |
| S39 | `GET /api/admin/audit` | traces CREATE/ATTRIBUTION/RETOUR... | 200, `AuditLog` trié `dateCreated desc` | OK |
| S40 | POST sans `X-CSRF-Token` | 400 "jeton de securite invalide" (CsrfFilter) | 400 | OK |
| S41 | En-têtes sécurité | CSP, X-Frame DENY, nosniff, HSTS | `SecurityHeadersFilter` | OK |
| S42 | Pagination `max/offset` + conservation `q/typeId` | `?q=&max=&offset=` | OK (`CatalogService`) | OK |

---

## Récapitulatif

42 scénarios (S1–S42) — **42 OK / 0 KO**. API JSON + front statique paritaires à la version Grails. Les 2 anomalies bloquantes de la v1 sont corrigées et rejouées (S10/S11). Limites résiduelles dans `LIMITES_CONNUES.md`.

## Données de test

Rollback transactionnel des tests Spock ; le seed `BootstrapSeed` reste inchangé après recette.
