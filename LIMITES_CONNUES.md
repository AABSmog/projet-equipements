# Limites connues

Document recensant les limites, points faibles et anomalies non traitées de l'application
de gestion d'équipements. Il complète les cahiers de recette (`cahiers-de-recette/`).
Mise à jour : v2 (2026-08-09).

## Anomalies traitées dans cette version (renforcement)

| # | Sujet | Correction |
|---|-------|------------|
| 1 | Suppression d'un personnel : la cascade `belongsTo` effaçait silencieusement tout l'historique (affectations + signalements) | Blocage : impossible de supprimer un personnel ayant un historique ; impossible de supprimer son propre compte |
| 2 | L'état d'un équipement était modifiable librement → incohérences possibles (AFFECTÉ sans affectation, DISPONIBLE avec affectation active) | Validateur de cohérence : AFFECTÉ exige une affectation active ; DISPONIBLE / HORS_SERVICE interdits tant qu'une affectation active existe ; un nouvel équipement est toujours créé DISPONIBLE |
| 3 | Mot de passe sans longueur minimale | `minSize: 6` (création et modification, y compris inscription publique) |
| 4 | Types d'équipement dupliquables avec casse différente (ex. « Scanner » / « scanner ») | Validateur d'unicité insensible à la casse (en plus du dédoublonnage par `findByNomIlike` dans le contrôleur) |
| 5 | Date de retour antérieure à la date d'affectation acceptée | Validateur `dateRetour >= dateAffectation` |
| 6 | Signalement sans type de problème | `type` obligatoire |
| 7 | `affecter` : si la sauvegarde de l'équipement échouait, l'affectation restait créée (orpheline) | Vérification des sauvegardes + rollback explicite de la transaction |
| 8 | Équipement sans type | `type` obligatoire (associé) |

## Anomalies et limites restantes (non traitées)

### Sécurité
- **Brute force sur la connexion** : aucune limitation de tentatives, aucun verrouillage de compte, aucun délai progressif.
- **Inscription publique ouverte** : pas de captcha, pas de confirmation d'adresse email, pas de limite de création de comptes.
- **Politique de mot de passe faible** : seule une longueur minimale de 6 caractères ; pas d'exigence de complexité (majuscule, chiffre, symbole), pas de liste noire des mots de passe courants. Le hachage BCrypt est correct.
- **Réinitialisation de mot de passe** : aucune procédure « mot de passe oublié » ; seul l'admin peut changer le mot de passe d'un compte.
- **Protection CSRF** : les formulaires POST ne sont pas protégés par un jeton CSRF (pas de Spring Security).
- **Authentification par session HTTP** : sessions en mémoire du conteneur, non distribuées ; aucune API/token (JWT, OAuth). Le cookie de session n'a pas de durée de vie configurée visible.

### Fiabilité / données
- **Suppression d'un équipement** : l'action `delete` (non exposée dans l'UI) détache les affectations et signalements puis supprime l'équipement ; le numéro de série peut alors être réutilisé. La suppression d'un type d'équipement est désactivée.
- **Pas de journal d'audit** : aucune trace dédiée des actions sensibles (création/modification/suppression admin) au-delà des logs applicatifs.
- **Pas de sauvegarde automatisée** de la base PostgreSQL (aucune procédure de backup/restore).
- **Conflits de modification concurrente** : les versions optimistes GORM existent mais les collisions ne sont pas gérées explicitement (l'utilisateur le plus récent écrase).
- **Pas de pagination** sur les listes (équipements, personnel, affectations, signalements).

### Fonctionnel
- **Signalement → état non automatisé** : un signalement (panne/réparation) ne modifie pas automatiquement l'état de l'équipement ; le passage à `EN_PANNE`/`REPARE` reste manuel (via l'édition) et non tracé.
- **États `EN_PANNE` / `REPARE` non bornés** : la cohérence est vérifiée uniquement pour `AFFECTÉ`, `DISPONIBLE` et `HORS_SERVICE` ; un équipement peut être passé en panne sans signalement préalable, ou réparé sans historique de panne.
- **Édition de l'état `EN_PANNE`/`REPARE` libre** dans le formulaire de modification (aucune règle métier associée).

### Messages et internationalisation
- **Messages par défaut en anglais** : certaines vues affichent les messages par défaut Grails (ex. « Property [motDePasse] ... is less than the minimum size of [6] ») en plus du message traduit ; `messages.properties` ne couvre pas tous les codes.
- **Couverture partielle du service de traduction** : certains codes de contrainte retombent sur le message générique « Une ou plusieurs saisies sont invalides. ».

### Tests
- **Aucun test automatisé** : la validation est manuelle via les cahiers de recette (`recette-v1.md`).

## Note
La base de données doit être conservée dans l'état « seed » décrit en fin de `cahiers-de-recette/recette-v1.md`
avant chaque exécution de recette.
