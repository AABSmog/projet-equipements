# Cahier de recette — Proj-Equipment Micronaut v3 (20 scénarios)

**Date :** 2026-09-01 — **Env :** `http://localhost:8080` (`EQUIPMENTS_DB_DDL=update`, `POSTGRES Equipments`)
**Comptes :** ADMIN `admin@testfinal.com` / `Mamadou2024!` (etab `test-final`), USER `aissatou.diallo@example.com` / `Aissatou24$` (principal) — tous uniformisés `USERS_MOTS_DE_PASSE.txt`
**Seed :** `BootstrapSeed.groovy:45` `assurerEquipementsPourTous` → 6/entreprise, `USERS_MOTS_DE_PASSE.txt` 31 comptes

| # | Module | Scénario (cas limite inclus) | Étapes | Résultat attendu | Statut |
|---|--------|------------------------------|--------|------------------|--------|
| S1 | Auth | Login ADMIN OK | `POST /api/auth/login {admin@testfinal.com, AdminFinal2024!, etablissementId:754}` | 200 `role ADMIN`, cookie `equipments-id` HttpOnly SameSite Lax, `csrfToken` rotaté `Api.js:76` | OK |
| S2 | Auth | Login USER OK | `POST /api/auth/login {aissatou.diallo@example.com, Aissatou24$}` | 200 `role USER`, redirection `/app/equipements.html` | OK |
| S3 | Auth | Mauvais mdp | `POST /api/auth/login` mauvais mdp | 401 `Email ou mot de passe incorrect` (`AuthController.groovy:74`) | OK |
| S4 | Auth | Bloqué après 5 échecs (brute-force) | 5× mauvais mdp même email/IP → 6e | 400 `Trop de tentatives...` (`LoginAttemptService.groovy:21`, `AuthController.groovy:45`) | OK |
| S5 | Auth | Register email libre | `POST /api/auth/register {nom,prenom,email,mdp:Realiste2024!}` | 200 `success` BCrypt `$2a$` (`Personnel.groovy:43`) | OK |
| S6 | Auth | Register email doublon (casse) | Même email `ADMIN@TESTFINAL.COM` | 400 `deja utilise` (`ValidationMessagesService.groovy:31` `lower(email)`) | OK |
| S7 | Auth | Register mdp faible | `mdp: "abc"` | 400 `8 caracteres minimum` (`Personnel.groovy:37`) | OK |
| S8 | Auth | Accès admin sans session | `GET /api/admin/stats` sans cookie | 401 `AuthFilter.groovy:19` | OK |
| S9 | Auth | USER tente admin | USER `GET /api/admin/personnels` | 403 `Accès interdit` | OK |
| S10 | Equipement | Création sans SN → auto `SN-…` | `POST /api/admin/equipements {typeId:1, description:"PC"}` sans `numeroSerie` | 200 `numeroSerie` `SN-XXXXXXXX` `DISPONIBLE` (`Equipement.groovy:36`) | OK |
| S11 | Equipement | Type `autre` crée type+équipement | `POST /api/admin/equipements {typeId:"autre", nouveauType:"Scanner 3D", ...}` | 200 2 inserts même transaction | OK |
| S12 | Equipement | Type manquant | `POST` sans `typeId` | 400 `Veuillez selectionner un type` (`AdminEquipementController.groovy:68`) | OK |
| S13 | Equipement | SN doublon (insensible casse) | Créer `SN-TEST-001` puis même `sn-test-001` | 400 `deja utilise` (`Equipement.groovy:54` `lower(numeroSerie)`) | **ANOMALIE Majeure A1** : message générique `deja utilise` sans champ |
| S14 | Equipement | Description vide | `POST {description:""}` | 400 `obligatoire` (`ValidationMessagesService`) | OK |
| S15 | Equipement | Forcer `HORS_SERVICE` à la création | `POST {etat:"HORS_SERVICE"}` | 400 `doit etre cree a l'etat disponible` (`Equipement.groovy:57`) | OK |
| S16 | Equipement | Passer à `AFFECTE` sans affectation | `PUT /{id} {etat:"AFFECTE"}` | 400 `sans affectation active` (`Equipement.groovy:60`) | OK |
| S17 | Affectation | Attribuer DISPONIBLE → USER | `POST /api/admin/affectations {equipementId, personnelId}` | 200 `Affectation` créée, `equipement.etat=AFFECTE`, `attribuePar=admin` (`AffectationService.groovy:37`) | OK |
| S18 | Affectation | Matériel déjà attribué (cas limite) | Même équipement à autre USER | 400 `n'est pas disponible` (`AffectationService.groovy:31`, `CatalogService` filtre `DISPONIBLE`) | OK |
| S19 | Affectation | Double attribution même couple | Même `equipementId+personnelId` | 400 `deja affecte a X` (`AffectationService.groovy:34`) | OK |
| S20 | Affectation | Restitution sans attribution (cas limite) | `POST /api/admin/affectations/{id}/retour` sur équipement DISPONIBLE | 400 `Aucune affectation active` (`AffectationService.groovy:83`) | **ANOMALIE Bloquante A2** corrigée — voir § Anomalies |

**Scénarios additionnels déroulés (hors 20, pour couverture) :**
- S21 EN_PANNE → attribution refusée 400 — OK
- S22 ids inexistants → 400 `introuvable` — OK
- S23 Desaffecter AFFECTE → 200 DISPONIBLE — OK
- S24 Double restitution → 400 `deja ete cloturee` — OK
- S25 Declasser AFFECTE → HORS_SERVICE + `infoEquipement` — OK
- S26 `dateRetour` antérieure → 400 `anterieure` — OK
- S27 Signalement propre équipement → 200 — OK
- S28 Signalement non affecté → 400 — OK

## Anomalies relevées (déroulé 2026-09-01)

| ID | Gravité | Scénario | Description | Statut |
|----|---------|----------|-------------|--------|
| A1 | Majeure | S13 | Message SN doublon trop générique, ne précise pas le champ (`numeroSerie`) | **Corrigé** : `ValidationMessagesService.groovy:54` → `Le numéro de série 'X' est déjà utilisé` |
| A2 | Bloquante | S20 | `restituer` sans affectation renvoyait 500 NPE au lieu de 400 ( `AffectationService.groovy:51` `affectation==null` → `introuvable`) | **Corrigé** : garde `if (!affectation) return [success:false, message:"Aucune affectation active pour cet équipement"]` |
| A3 | Majeure | S6 | Email doublon sensible à la casse avant `lower()` — déjà corrigé `Personnel.groovy:31` | OK (vérifié) |
| A4 | Mineure | UI | `Base = Entreprise` et `equipments_<slug>` exposaient le nom interne DB | **Corrigé** : `index.html:13` `Choisissez votre entreprise`, `etablissement-wizard.html:52` `Votre espace sera isolé` |
| A5 | Mineure | UI | `Slug` exposé en `Identifiant (slug)` | **Corrigé** : `Code (auto)` `Entreprise` |
| A6 | Mineure | API | `GString` `message` sérialisé en objet `{blank,bytes}` → `Flash` KO | **Corrigé** : `EtablissementService.groovy:136` `.toString()` |
| A7 | Mineure | Sécu | `POST /api/auth/login` exigeait CSRF sans session → `jeton invalide` systématique | **Corrigé** : `CsrfFilter.groovy:40` exempte `login|register` + `login.js:11` `await Api.initSession()` |
| A8 | Mineure | Sécu | `Api.js` ne mettait pas à jour `csrfToken` après rotation login | **Corrigé** : `Api.js:76` + `AuthController.groovy:68` renvoie `csrfToken` |

**Restant à documenter dans `LIMITES_CONNUES.md` :** paginations max 100, session mémoire non distribuée, pas de mot de passe oublié, `tailwind.css` précompilé.
