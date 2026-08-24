# Cahier de recette — Gestion des équipements

Version : v1
Application : Gestion des équipements (Grails 7 / PostgreSQL)
Environnement : développement — `http://localhost:18080`
Date d'exécution : 06/08/2026

Comptes de test :
| Rôle  | Email                        | Mot de passe |
|-------|------------------------------|--------------|
| ADMIN | mamadou.diop@example.com     | admin123     |
| USER  | aissatou.diallo@example.com  | pass123      |

Légende : OK = conforme / KO = anomalie.

---

## 1. Authentification

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S1 | Connexion admin valide | redirect `/admin` | `302 → /admin` | OK |
| S2 | Connexion user valide | redirect `/app` | `302 → /app` | OK |
| S3 | Mot de passe incorrect | rejet + message | `200`, message « Email ou mot de passe incorrect » | OK |
| S4 | Email inconnu | rejet + message | `200`, message affiché (pas de fuite d'info) | OK |
| S5 | Inscription valide (email saisi) | compte créé + session | `302 → /app`, mdp hashé BCrypt (`$2a$10$`) | OK |
| S6 | Inscription email déjà utilisé | refus + message | `200`, erreur « must be unique » affichée | OK* |

\* S6 : fonctionnel mais message technique en anglais affiché tel quel (voir anomalie M3).

## 2. Équipement

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S7 | Créer avec un type existant (sans SN) | création + SN auto-généré | `200`, « Property [numeroSerie] cannot be null » | **KO** |
| S8 | Créer avec un nouveau type « Autre... » | type + équipement créés | `200`, même erreur SN ; le type est créé seul | **KO** |
| S9 | Créer sans type | refus « sélectionnez un type » | `302 → create`, flash correct | OK |
| S10 | « Autre... » avec nouveau type vide | refus « saisir le nom » | `302 → create`, flash correct | OK |
| S11 | N° de série dupliqué | refus (unicité) | `200`, « must be unique » | OK |
| S12 | Description vide | refus | `200`, « cannot be null » | OK |
| S7b | Créer avec type existant ET SN fourni | création | `302 → list`, équipement créé (SN-REC-S7B) | OK |

**Analyse S7/S8** : le formulaire `create.gsp` n'a pas de champ `numeroSerie` ; l'auto-génération se fait dans `beforeInsert()`, or la contrainte `numeroSerie blank: false` (non-nullable par défaut) fait échouer la validation AVANT l'insertion → `beforeInsert` n'est jamais appelé. La création d'un équipement est **impossible via l'UI** tant qu'aucun SN n'est fourni.

## 3. Affectation

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S13 | Affecter un équipement DISPONIBLE | affectation + état AFFECTÉ | eq 15 → AFFECTÉ, affectation 43 active | OK |
| S14 | **Matériel déjà attribué** (eq AFFECTÉ) | refus, pas de double | eq 15 reste AFFECTÉ, aucune 2e affectation | OK |
| S15 | Affecter un équipement EN_PANNE | refus | eq 17 inchangé (EN_PANNE), aucune affectation | OK |
| S16 | Équipement / personnel inexistant | refus | aucun impact, flash d'erreur | OK |
| S17 | Retour admin avec raison | date de retour + état DISPONIBLE | affectation 43 close, eq 15 → DISPONIBLE, raison enregistrée | OK |
| S18 | Retour employé sur équipement affecté | retour + état DISPONIBLE | affectation 44 close, eq 16 → DISPONIBLE | OK |
| S19 | **Restitution sans attribution** | rejet « aucune affectation active » | eq 15 inchangé, redirect `/app`, flash | OK |
| S20 | Désaffecter un équipement non affecté | rejet « n'est pas affecté » | eq 16 inchangé | OK |

## 4. Signalement

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S21 | Signaler un problème sur son équipement | signalement créé | `302 → /app/equipement/show/18`, signalement 46 créé | OK |
| S22 | Signaler un équipement non affecté | rejet | `302 → /app/equipement/list`, flash | OK |
| S23 | Description vide | rejet | `200`, aucun signalement créé, flash générique | OK* |
| S24 | Type de signalement invalide | rejet propre | `200`, flash « Type de signalement invalide », pas d'exception | OK |

\* S23 : rejet correct mais message générique ne précisant pas la cause (voir anomalie M4).

## 5. Contrôle d'accès et déclassement

| ID | Scénario | Attendu | Obtenu | Statut |
|----|----------|---------|--------|--------|
| S25 | USER accède à `/admin` | redirection `/app` | `302 → /app` | OK |
| S26 | Anonyme sur `/admin` puis `/app` | redirection `/login` | `302 → /login` (les deux) | OK |
| S27 | Déclasser un équipement AFFECTÉ | affectation clôturée + HORS_SERVICE | affectation 47 close + `info_equipement` renseigné, eq 19 → HORS_SERVICE | OK |
| S28 | Déclasser un équipement DISPONIBLE | HORS_SERVICE | eq 20 → HORS_SERVICE | OK |
| — | Page de confirmation `declasserConfirm` | affiche détail + form | rendu correct avec bouton de confirmation | OK |

---

## Récapitulatif

28 scénarios exécutés — **26 OK** / **2 KO** (S7, S8).

| # | Anomalie | Scénario | Gravité | Issue |
|---|----------|----------|---------|-------|
| A1 | Création d'équipement impossible via l'UI : `numeroSerie` non fourni par le formulaire, validation « cannot be null » avant `beforeInsert` → l'auto-génération du SN ne fonctionne jamais | S7 | **MAJEURE** | [#1](https://github.com/AABSmog/projet-equipements/issues/1) |
| A2 | Type créé « orphelin » quand la création d'équipement échoue après un « Autre... » (le type reste en base sans équipement) | S8 | **MAJEURE** | [#2](https://github.com/AABSmog/projet-equipements/issues/2) |
| A3 | Message d'erreur technique en anglais exposé à l'utilisateur (« Property [email] ... must be unique », « cannot be null », « must be unique ») | S6, S11, S12 | Mineure | [#3](https://github.com/AABSmog/projet-equipements/issues/3) |
| A4 | Message générique « Erreur lors de l'envoi du signalement » sans indiquer la cause (description obligatoire) | S23 | Mineure | [#4](https://github.com/AABSmog/projet-equipements/issues/4) |

> Convention : chaque cahier de recette est versionné (`recette-vN.md`). Les cahiers successifs sont enregistrés dans ce dossier à chaque campagne de test. Chaque anomalie est tracée dans une issue GitHub.

## Données de test créées pendant la recette (base dev)

- `Personnel` : recette.valid@example.com (S5)
- `TypeEquipement` : « Moniteur 4K » (S8 — orphelin, lié à A2)
- `Equipement` : SN-REC-S7B (S7b)
- `Affectation` : ids 43, 44, 47 (closes)
- `Signalement` : id 46 (S21)
