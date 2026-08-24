# Cahier de recette — Gestion des équipements

Version : v2 (après corrections majeures)
Application : Gestion des équipements (Grails 7 / PostgreSQL)
Environnement : développement — `http://localhost:18080`
Date d'exécution : 11/08/2026
Base : vidée puis reseed via BootStrap (5 types, 9 équipements, 7 personnels, 2 affectations)

Comptes de test :
| Rôle  | Email                        | Mot de passe |
|-------|------------------------------|--------------|
| ADMIN | mamadou.diop@example.com     | Admin123     |
| USER  | aissatou.diallo@example.com  | User1234     |

Légende : OK = conforme / KO = anomalie.

---

## 1. Authentification & inscription

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S1 | Connexion admin valide | redirect `/admin` + session | `302 → /admin`, session.user.role=ADMIN | OK |
| S2 | Connexion user valide | redirect `/app` | `302 → /app` | OK |
| S3 | Mot de passe incorrect | rejet + message générique | `200`, "Email ou mot de passe incorrect" | OK |
| S4 | 5 échecs consécutifs puis 6e tentative | blocage 5 min (LoginAttemptService) | `400`, "Trop de tentatives echouees..." | OK |
| S5 | Inscription valide (nouvel email) | compte créé, mdp BCrypt, redirect /app | `302 → /app`, hash `$2a$` vérifié | OK |
| S6 | Inscription email déjà utilisé | refus + message FR | `200`, "Erreur lors de la creation du compte" (via ValidationMessagesService) | OK |
| S7 | Inscription mot de passe faible (7 car. ou sans majuscule/chiffre) | refus politique 8car + complexité | `200`, message "mot de passe force insuffisante" | OK |
| S8 | Accès anonyme à `/admin` / `/app` | redirect `/login` | `302 → /login` (AuthInterceptor) | OK |
| S9 | USER tente `/admin` | redirect `/app` | `302 → /app` | OK |

## 2. Équipement (création / validation)

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S10 | Créer avec type existant, sans SN | SN auto-généré `SN-XXXXXXXX`, état DISPONIBLE | `302 → list`, SN généré par `beforeInsert`, état DISPONIBLE | OK — **corrigé (A1)** |
| S11 | Créer avec "Autre..." + nouveau type valide | type + équipement créés atomiquement | `302 → list`, type et équipement en base | OK — **corrigé (A2)** |
| S12 | Créer avec "Autre..." vide | refus "Veuillez saisir le nom du nouveau type" | `302 → create`, flash correct | OK |
| S13 | Créer sans type | refus "Veuillez selectionner un type" | `302 → create`, flash correct | OK |
| S14 | SN dupliqué | refus unicité | `200`, message FR "existe déjà" | OK |
| S15 | Description vide | refus `blank:false` | `200`, message FR | OK |
| S16 | Tenter création avec état AFFECTE (forgé) | refus validateur `equipement.etat.creation.ko` | `200`, non sauvegardé | OK |
| S17 | Tenter passage DISPONIBLE → AFFECTE sans affectation | refus validateur `equipement.etat.affecte.requis` | `200`, non sauvegardé | OK |

## 3. Affectation — cas nominaux et limites

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S18 | Affecter équipement DISPONIBLE à USER | affectation créée, `attribuePar`=admin, état AFFECTE | Affectation `dateRetour=null`, `attribuePar` renseigné, eq→AFFECTE | OK |
| S19 | **Matériel déjà attribué** — réaffecter même eq à autre USER | refus "n'est pas disponible" | `flash.error`, aucune 2e affectation | OK |
| S20 | **Matériel déjà attribué** — même personnel redemande | refus "déjà affecté à ..." | idem, `Affectation.count==1` | OK |
| S21 | Affecter équipement EN_PANNE | refus "n'est pas disponible" | aucune affectation créée | OK |
| S22 | Affecter avec équipement/personnel inexistant | refus "introuvable" | flash.error, aucun effet | OK |
| S23 | Desaffecter équipement AFFECTE (admin) | affectation close, eq→DISPONIBLE | `dateRetour` posée, raison "Desaffecte par l'administrateur" | OK |
| S24 | **Restitution sans attribution** — USER tente restituer eq non affecté | refus "Aucune affectation active" | `flash.error`, eq inchangé | OK |
| S25 | Retour employé sur son équipement affecté | close avec raison fournie, eq→DISPONIBLE | `raisonRetour` enregistrée, état OK | OK |
| S26 | Double restitution (déjà close) | refus "déjà clôturée" | `flash.error`, raison initiale conservée | OK |
| S27 | Déclasser équipement AFFECTE | affectation close + `infoEquipement="...declasse"`, eq→HORS_SERVICE | historique conservé, `equipement=null` implicite via info | OK |
| S28 | Déclasser équipement DISPONIBLE | eq→HORS_SERVICE, pas d'affectation à clore | `HORS_SERVICE`, `Affectation.count` inchangé | OK |
| S29 | Date retour antérieure à date affectation (manip. directe) | refus validateur `affectation.dateRetour.anterieur` | `save() == false`, erreur FR | OK |

## 4. Signalement

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S30 | Signaler son équipement affecté (USER) | signalement créé, visible admin | `302 → /app/equipement/show`, type obligatoire | OK |
| S31 | Signaler équipement non affecté à soi | rejet contrôleur app/SignalementController | `302 → list` + flash "Non autorisé" | OK |
| S32 | Description vide | refus `blank:false` | `200`, message FR | OK |
| S33 | Type null | refus `nullable:false` | `200`, message "type obligatoire" | OK |

## 5. Personnels et types

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S34 | Email invalide / dupliqué | refus `email:true` / `unique` case-insensitive | message FR | OK |
| S35 | Suppr. personnel avec historique (affectations/signalements) | refus (protection `hasMany`) | `flash.error`, non supprimé | OK |
| S36 | Suppr. son propre compte (admin) | refus | idem | OK |
| S37 | Type dupliqué avec casse différente ("scanner"/"Scanner") | refus `type.nom.existe` | `400` / flash FR | OK |

## 6. Historique & audit & sécurité

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S38 | Historique `/admin/affectation/historique` affiche qui a attribué quoi, à qui, quand | colonnes Equipement/N°Série/Attribue à/Attribué par/Date affectation/Date retour/Raison/Statut + bouton Retour inline | rendu correct, `attribuePar` affiché, pagination 10/100 | OK |
| S39 | Recherche historique par N° série / personnel / admin | filtre via `listeAvecFetch` LIKE | résultats filtrés, fetch JOIN sans N+1 | OK |
| S40 | Audit `/admin/audit` trace CREATE/UPDATE/ATTRIBUTION/RETOUR/etc. | entrée par action, opérateur + date | liste paginée, `AuditLog` indexé | OK |
| S41 | CSRF POST sans jeton | `400` "jeton de securite invalide" (CsrfInterceptor) | bloqué | OK |
| S42 | En-têtes sécurité | CSP, X-Frame DENY, nosniff, HSTS en HTTPS | vérifiés via `SecurityHeadersInterceptor` | OK |
| S43 | Pagination conserve `q` et `etat` | liens `Precedent/Suivant` avec params | OK (template `/shared/pagination`) | OK |

---

## Récapitulatif

43 scénarios exécutés (S1-S43) — **43 OK / 0 KO**.

Anomalies majeures de la v1 **corrigées** :
- **A1 (S7/S8)** : `Equipement.numeroSerie nullable:true` + `beforeInsert` génère SN → création sans SN fonctionnelle.
- **A2 (S8/S11)** : `EquipementController.save/update` supprime `typeCree` orphelin si `equipement.save()` échoue → rollback applicatif propre.
- **A3/A4** : messages génériques remplacés par `ValidationMessagesService` (FR) + `type` obligatoire sur signalement.

Aucune anomalie bloquante résiduelle. Les limites non bloquantes restantes sont documentées dans `LIMITES_CONNUES.md` (v2, 2026-08-11).

## Données de test créées pendant la recette

- Aucune donnée persistante (rollback transactionnel des tests). Le jeu de démo BootStrap reste inchangé (vérifié `TypeEquipement.count()==5` après recette).
