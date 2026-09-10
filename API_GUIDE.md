# Guide des endpoints API — Proj-Equipment Micronaut

Base URL: `http://localhost:8080` (cf. `src/main/resources/application.yml:6`)
`Content-Type: application/json` — Réponses `application/json`. Session via cookie `equipments-id` (`src/main/groovy/proj/equipment/service/SessionManager.groovy:21`). CSRF via `X-CSRF-Token` (sauf `/api/etablissements/wizard`, `/api/auth/login|register` — `CsrfFilter.groovy:40`).

Auth via `SessionManager` — `AuthFilter.groovy:19` protège `/api/admin/**` (ADMIN) et `/api/app/**` (connecté). Tenant via `TenantFilter.groovy:14` (`/api/**`) → `TenantContext.groovy:13` (filtrage `etablissement_id`).

---

## 1. Auth — `AuthController.groovy:14` `@Controller('/api/auth')`

### `GET /api/auth/session` — `AuthController.groovy:23`
Public. Crée/recupère session, génère `csrfToken` si absent.
```bash
curl -c cjar.txt http://localhost:8080/api/auth/session
# => {"user":null|{id,email,role,etablissement:{id,nom,slug}}, "csrfToken":"uuid"}
```

### `POST /api/auth/login` — `AuthController.groovy:38` — **CSRF exempt** depuis `CsrfFilter.groovy:40`
Body: `{"email","password","etablissementId"?,"etablissementSlug"?}` — `etablissementId` ou `slug` résout l'entreprise (`AuthController.groovy:49`). Vérifie `etablissement.id` (`AuthService.groovy:26` `left join fetch`).
```bash
curl -b cjar.txt -c cjar.txt -X POST -H "X-CSRF-Token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"email":"admin@testfinal.com","password":"AdminFinal2024!","etablissementId":754}' \
  http://localhost:8080/api/auth/login
# 200 => {"user":{...},"csrfToken":"new-uuid"} — token rotaté, `Api.js:76` le met à jour
# 401 => {"error":"Email ou mot de passe incorrect"}
# 400 => {"error":"Trop de tentatives..."} (LoginAttemptService, 5 essais / 15min)
```

### `POST /api/auth/logout` — `AuthController.groovy:78`
```bash
curl -b cjar.txt -X POST -H "X-CSRF-Token: $TOKEN" http://localhost:8080/api/auth/logout
# => {"message":"Deconnecte"} — cookie expiré
```

### `POST /api/auth/register` — `AuthController.groovy:85` — **CSRF exempt**
Body: `{"nom","prenom","email","motDePasse"}`
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"nom":"Diop","prenom":"Mamadou","email":"m@ex.com","motDePasse":"Mamadou2024!"}' \
  http://localhost:8080/api/auth/register
# Limite 3 / heure / IP
```

---

## 2. Entreprises (Etablissements) — public & admin

### Public — `PublicEtablissementController.groovy:10` `@Controller('/api/etablissements')` — **sans auth**

| Méthode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/etablissements` `PublicEtablissementController.groovy:15` | Liste toutes (`id,nom,slug,domaineEmail,statut`) |
| `GET` | `/api/etablissements/{id}` `PublicEtablissementController.groovy:22` | Détail, 404 `Entreprise introuvable` |
| `POST` | `/api/etablissements/wizard` `PublicEtablissementController.groovy:29` | **Création publique 4 étapes**, CSRF exempt. Body `EtablissementService.groovy:51` : `{"etablissement":{"nom","slug"?,"domaineEmail"},"admins":[{"nom","prenom","email"?,"motDePasse"}],"employes":[],"regles":{}}` → `slug` auto via `slugify` si vide, `domaineEmail` défaut `example.com`. |

```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"etablissement":{"nom":"Atelier Dakar","slug":"atelier-dakar","domaineEmail":"atelier.sn"},"admins":[{"nom":"Diop","prenom":"Mamadou","motDePasse":"Mamadou2024!"}],"employes":[],"regles":{}}' \
  http://localhost:8080/api/etablissements/wizard
# => {"success":true,"message":"Entreprise 'Atelier Dakar' créée : 1 administrateur(s)...","etablissement":{id, nom, slug},"admins":[...]}
# => GString fix `EtablissementService.groovy:136` `.toString()` évite objet `bytes`
```

### Admin — `AdminEtablissementController.groovy:11` `@Controller('/api/admin/etablissements')` — **ADMIN**

| Méthode | Path | Description |
|---------|------|-------------|
| `GET` | `/api/admin/etablissements` `AdminEtablissementController.groovy:16` | Liste détaillée (+ `dbUrl`, `dateCreation`) |
| `GET` | `/api/admin/etablissements/{id}` `AdminEtablissementController.groovy:25` | Détail |
| `GET` | `/api/admin/etablissements/slug/{slug}` `AdminEtablissementController.groovy:32` | Par slug |
| `POST` | `/api/admin/etablissements/wizard` `AdminEtablissementController.groovy:39` | Idem public mais nécessite `X-CSRF-Token` + `ADMIN` (création depuis dashboard) |
| `GET` | `/api/admin/etablissements/preview-email?prenom=&nom=&etablissementId=` `AdminEtablissementController.groovy:52` | `{"email":"prenom.nom@ex.com"}` via `RegleGestionService` |

---

## 3. Règles par entreprise — `AdminRegleController.groovy:12` `@Controller('/api/admin/etablissements/{etablissementId}/regles')` — **ADMIN**

| Méthode | Path |
|---------|------|
| `GET` | `/api/admin/etablissements/{id}/regles` `AdminRegleController.groovy:18` → `{"etablissement":{id,nom,slug},"regles":[{cle,valeur,label,type}]}` |
| `PUT` | `/api/admin/etablissements/{id}/regles` `AdminRegleController.groovy:26` Body `{"regles":{"password.minLength":"8",...}}` ou `{"cle":"val"}` |
| `GET` | `/api/admin/etablissements/{id}/regles/preview-email?prenom=&nom=` `AdminRegleController.groovy:46` |

```bash
curl -b cjar.txt -H "X-CSRF-Token: $NEWTOKEN" http://localhost:8080/api/admin/etablissements/754/regles
curl -b cjar.txt -X PUT -H "X-CSRF-Token: $NEWTOKEN" -H "Content-Type: application/json" \
  -d '{"regles":{"password.minLength":"10"}}' http://localhost:8080/api/admin/etablissements/754/regles
```

---

## 4. Equipements — Admin `AdminEquipementController.groovy:17` `@Controller('/api/admin/equipements')` — **ADMIN + Tenant**

Isolation `TenantContext.groovy:21` `etablissement_id` sur toutes les requêtes.

| Méthode | Path | Query/Body |
|---------|------|------------|
| `GET` | `/api/admin/equipements?q=&etat=&max=10&offset=0` `AdminEquipementController.groovy:30` | `CatalogService.groovy:35` → `{total, items:[{id,numeroSerie,description,etat,type}]}` |
| `GET` | `/api/admin/equipements/{id}` `AdminEquipementController.groovy:38` | + `affecte`, `signalements` |
| `GET` | `/api/admin/equipements/{id}/declasser-confirm` `AdminEquipementController.groovy:52` | |
| `POST` | `/api/admin/equipements` `AdminEquipementController.groovy:64` | Body `{"typeId":"1"|"autre","nouveauType":"...","numeroSerie","description","etat"?}` — `etablissement: TenantContext.get()` |
| `PUT` | `/api/admin/equipements/{id}` `AdminEquipementController.groovy:86` | `numeroSerie` non modifiable `AdminEquipementController.groovy:100` |
| `POST` | `/api/admin/equipements/{id}/desaffecter` `AdminEquipementController.groovy:119` | |
| `POST` | `/api/admin/equipements/{id}/declasser` `AdminEquipementController.groovy:133` | |
| `DELETE` | `/api/admin/equipements/{id}` `AdminEquipementController.groovy:147` | Bloqué si `AFFECTE`, détache affectations/signalements |

---

## 5. Personnels — `AdminPersonnelController.groovy:18` `@Controller('/api/admin/personnels')` — **ADMIN + Tenant**

| Méthode | Path |
|---------|------|
| `GET` | `/api/admin/personnels?q=&max=10&offset=0` `AdminPersonnelController.groovy:30` |
| `GET` | `/api/admin/personnels/{id}` `AdminPersonnelController.groovy:37` |
| `POST` | `/api/admin/personnels` `AdminPersonnelController.groovy:45` Body `{"nom","prenom","email","motDePasse","role":"ADMIN|USER"}` — `etablissement: TenantContext.get()` |
| `PUT` | `/api/admin/personnels/{id}` `AdminPersonnelController.groovy:62` |
| `DELETE` | `/api/admin/personnels/{id}` `AdminPersonnelController.groovy:83` — Protège dernier admin, historique |

---

## 6. Affectations — `AdminAffectationController.groovy:14` `@Controller('/api/admin/affectations')` — **ADMIN**

| Méthode | Path |
|---------|------|
| `GET` | `/api/admin/affectations?q=&typeId=0&max=10&offset=0` `AdminAffectationController.groovy:26` |
| `GET` | `/api/admin/affectations/historique?q=&typeId=&max=&offset=` `AdminAffectationController.groovy:34` — même `CatalogService.listeAffectations` |
| `POST` | `/api/admin/affectations` `AdminAffectationController.groovy:42` Body `{"equipementId","personnelId"}` → `AffectationService.groovy:29` vérifie `etablissement` |
| `POST` | `/api/admin/affectations/{id}/retour` `AdminAffectationController.groovy:55` Body `{"raisonRetour"}` |

---

## 7. Signalements

**Admin** `AdminSignalementController.groovy:16` `@Controller('/api/admin/signalements')` — **ADMIN**
| `GET` | `/api/admin/signalements?q=&max=10&offset=0` `AdminSignalementController.groovy:28` |
| `DELETE` | `/api/admin/signalements/{id}` `AdminSignalementController.groovy:35` |

**App** `AppSignalementController.groovy:14` `@Controller('/api/app/signalements')` — **connecté (USER/ADMIN)**
| `GET` | `/api/app/signalements?max=&offset=` `AppSignalementController.groovy:27` → `mesSignalements` |
| `GET` | `/api/app/signalements/creation?equipementId=` `AppSignalementController.groovy:35` — vérifie affectation active |
| `POST` | `/api/app/signalements` `AppSignalementController.groovy:48` Body `{"equipementId","type":"PANNE|PROBLEME_FONCTIONNEL|...","description"}` |

---

## 8. Equipements — App (utilisateur) `AppEquipementController.groovy:14` `@Controller('/api/app/equipements')`

| `GET` | `/api/app/equipements?max=&offset=` `AppEquipementController.groovy:25` → `mesEquipements` (filtré par `etablissement` + affecté) |
| `GET` | `/api/app/equipements/{id}` `AppEquipementController.groovy:33` — 403 si non affecté |
| `POST` | `/api/app/equipements/{id}/retour` `AppEquipementController.groovy:46` Body `{"raisonRetour"}` |

---

## 9. Recherche — `AdminRechercheController.groovy:8` `@Controller('/api/admin/recherche')` — **ADMIN**

| `GET` | `/api/admin/recherche/equipements?q=&typeId=` `AdminRechercheController.groovy:14` → `CatalogService.rechercherEquipementsDisponibles` |
| `GET` | `/api/admin/recherche/personnels?q=` `AdminRechercheController.groovy:21` |

---

## 10. Types — `AdminTypeController.groovy:10` `@Controller('/api/admin')` — **ADMIN**

| `GET` | `/api/admin/types` `AdminTypeController.groovy:15` → `catalogService.listeTypes()` |
| `GET` | `/api/admin/types/list?q=&max=20&offset=0` `AdminTypeController.groovy:20` |
| `POST/PUT/DELETE` | `/api/admin/types` `AdminTypeController.groovy:29` → 400 `"Autre..."` (création via `typeId:"autre"` dans equipement) |

---

## 11. Audit & Stats

| Méthode | Path | Contrôleur |
|---------|------|------------|
| `GET` | `/api/admin/audit?q=&max=20&offset=0` | `AdminAuditController.groovy:16` |
| `GET` | `/api/admin/stats` | `AdminController.groovy:15` → `CatalogService.statsAdministration()` `{totalEquipements, disponibles, affectes, signalements...}` |

---

## 12. Meta — `MetaController.groovy:11` `@Controller('/api/meta')` — **public**

```bash
curl http://localhost:8080/api/meta
# => {"etatsEquipement":[{"value":"DISPONIBLE","label":"Disponible"},...],"typesSignalement":[...],"roles":[...]}
```

---

## Auth & CSRF — résumé `api.js:1`

```js
await Api.initSession() // GET /api/auth/session → {csrfToken}
await Api.post('/api/auth/login', {email,password,etablissementId, etablissementSlug})
// login exempt CSRF (CsrfFilter.groovy:40) + rotation token (AuthController.groovy:66, Api.js:76)
headers = {'X-CSRF-Token': Api.csrf(), 'Content-Type':'application/json'}
```

Erreurs : `400 Session expirée...` (CsrfFilter.groovy:45), `401 Email ou mot de passe incorrect`, `403 Accès interdit` (AuthFilter.groovy:19), `404 Entreprise/Equipement introuvable`.
Tenant : `AuthController.groovy:64` stocke `etablissementId/Slug` en session → `TenantFilter.groovy:33` → `TenantContext`.

Guide généré depuis `src/main/groovy/proj/equipment/controller/*.groovy` et `src/main/resources/static/js/api.js:1`.
