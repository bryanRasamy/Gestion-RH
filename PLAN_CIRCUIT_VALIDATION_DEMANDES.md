# Plan d'implémentation — Circuit de validation des demandes

Ce document adapte le sprint « circuit de validation des demandes » à l'application existante `Gestion-RH`. Il décrit la cible technique et l'ordre de réalisation, sans modifier encore le code applicatif.

## 1. État de départ du projet

L'application est un monolithe Spring Boot / Thymeleaf connecté à PostgreSQL.

Le flux déjà commencé est :

```text
Manager connecté
  -> crée une demande et ses lignes de détail
  -> la demande reçoit un historique de statut initial
  -> liste des demandes
```

Les éléments existants à réutiliser sont :

- `Demande`, `DetailDemande`, `Status`, `HistoriqueDemandeStatus` et leurs repositories ;
- `DemandeService`, qui sauvegarde une demande et ses détails dans une transaction ;
- `DemandeController`, pour l'espace manager ;
- `Utilisateur`, `Employer`, `Poste` et `Departement` pour l'identité métier ;
- la table `historique_demande_status`, qui peut devenir l'audit officiel de toutes les décisions.

Les fonctionnalités RH, Finance et DG ne sont pas encore codées. Les redirections correspondantes existent partiellement, mais les routes et écrans associés n'existent pas encore.

## 2. Décisions d'architecture à figer

### 2.1 Rôles

Le code actuel déduit des droits depuis `poste.sigle` et `departement.sigle`. Cette règle est insuffisante : elle ne définit pas clairement Finance et mélange un poste avec une autorisation applicative.

Ajouter un rôle explicite sur `utilisateur` :

```text
CANDIDAT | MANAGER | RH | FINANCE | DG
```

Les données de démonstration doivent ensuite associer les comptes existants aux rôles nécessaires, et inclure un compte Finance. L'authentification conservera l'employé associé afin de savoir qui a effectué une action.

### 2.2 Les cinq statuts

Le référentiel `status` ne contient que les cinq statuts fonctionnels suivants :

```text
CREE | VALIDE | REFUSE | ABANDONNE | EXPIRE
```

`VALIDE` est volontairement générique. L'étape de validation (RH, Finance ou DG) est enregistrée dans l'historique, pas dans le statut lui-même.

### 2.3 Historique unique des décisions

Il ne faut pas créer une deuxième table pour le statut de demande : la table existante `historique_demande_status` répond déjà à ce besoin. Elle devient la source de vérité et reçoit une nouvelle ligne à chaque action. Les anciennes lignes ne sont jamais modifiées ou supprimées.

Structure cible :

```text
demande 1 ---< historique_demande_status >--- 1 status
                         |
                         +--- 0..1 employer (auteur de l'action)
```

Colonnes à conserver ou ajouter :

| Colonne | Utilité |
| --- | --- |
| `id` | ordre fiable des événements |
| `id_demande` | demande concernée |
| `id_status` | l'un des cinq statuts |
| `id_employer` | auteur humain ; nul pour le système |
| `etape` | `CREATION`, `RH`, `FINANCE`, `DG`, `MANAGER`, `SYSTEME` |
| `date_action` | date et heure exactes |
| `commentaire` | motif de refus ou commentaire métier |

Le statut courant est la dernière ligne selon le plus grand `id` :

```sql
SELECT *
FROM historique_demande_status
WHERE id_demande = :idDemande
ORDER BY id DESC
LIMIT 1;
```

Le plus grand `id` est préférable à une date seule : la colonne `date` actuelle ne contient pas d'heure et ne permet pas de départager deux actions effectuées le même jour.

Exemple d'audit :

| Ordre | Statut | Étape | Auteur |
| ---: | --- | --- | --- |
| 1 | `CREE` | `CREATION` | Manager |
| 2 | `VALIDE` | `RH` | Responsable RH |
| 3 | `VALIDE` | `FINANCE` | Responsable Finance |
| 4 | `VALIDE` | `DG` | Directeur général |

Avec ce modèle :

- `CREE` attend une action RH ;
- `VALIDE / RH` attend Finance ;
- `VALIDE / FINANCE` attend DG ;
- `VALIDE / DG` est final et déclenche la création des offres ;
- `REFUSE`, `ABANDONNE` et `EXPIRE` sont finaux.

Le champ `etape` est un instantané d'audit. Il ne faut pas seulement relire le rôle actuel de l'auteur, car ce rôle pourrait changer ultérieurement.

## 3. Gestion d'une demande « à revoir »

« À revoir » n'est pas un statut puisque les statuts sont limités à cinq. C'est une correction ouverte dans une table `demande_a_revoir`.

Une correction contient :

- l'employé demandeur ;
- le détail de demande concerné ;
- un commentaire ;
- un nombre de personnel proposé, facultatif ;
- une date ;
- un indicateur `traite`.

Pendant une correction ouverte :

- le statut de demande reste inchangé ;
- l'étape qui a demandé la correction ne peut pas valider ni refuser ;
- le manager peut voir, modifier puis traiter les lignes demandées ;
- après traitement, les lignes passent à `traite = true` ;
- la demande revient à l'étape qui avait demandé la correction.

Exemple : Finance demande une correction. La demande reste `VALIDE / RH`, mais Finance est bloquée tant que la correction ouverte n'est pas traitée. Après correction, Finance peut à nouveau prendre une décision.

## 4. Transitions métier

| État courant | Acteur autorisé | Actions |
| --- | --- | --- |
| `CREE / CREATION` | RH | valider, refuser, demander une correction |
| `VALIDE / RH` | Finance | valider, refuser, demander une correction |
| `VALIDE / FINANCE` | DG | valider, refuser, demander une correction |
| correction ouverte | Manager | corriger ou abandonner |
| `VALIDE / DG` | aucun | état final, offres créées |
| `REFUSE`, `ABANDONNE`, `EXPIRE` | aucun | états finaux |

L'expiration concerne une demande non finale dont `date_limite < aujourd'hui`. Elle ajoute une ligne `EXPIRE / SYSTEME` avec `id_employer = NULL`.

## 5. Migrations SQL

Le schéma actuel est fourni par `database/table.sql`. Les évolutions doivent être écrites dans des scripts versionnés, par exemple :

```text
database/migrations/
  01_roles_et_statuts.sql
  02_demande_revision_offre.sql
  03_budget.sql
  99_donnees_test.sql
```

Évolutions principales :

```sql
ALTER TABLE utilisateur ADD COLUMN role VARCHAR(20);

ALTER TABLE historique_demande_status
  ALTER COLUMN id_employer DROP NOT NULL;

ALTER TABLE historique_demande_status
  ADD COLUMN etape VARCHAR(20),
  ADD COLUMN commentaire TEXT,
  ADD COLUMN date_action TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
```

Créer aussi :

- `demande_a_revoir` ;
- `offre`, avec `UNIQUE(id_detail_demande)` ;
- `grille_budget` ;
- `parametre` avec le code `BUDGET` ;
- une vue `v_demande_statut_courant` basée sur le dernier historique par `id`.

La clé étrangère manquante `detail_demande.id_diplome -> diplome.id` doit également être ajoutée.

En production, éviter `spring.jpa.hibernate.ddl-auto=update` : la structure doit être maîtrisée par les migrations SQL.

## 6. Services métier à créer

```text
security/
  Role
  AutorisationService

service/
  DemandeStatutService
  DemandeARevoirService
  OffreService
  BudgetService
```

Contrats recommandés :

```java
statutCourant(idDemande)
listerParEtape(etape)
changer(idDemande, statutAttendu, nouveauStatut, etape, acteur, commentaire)
demanderRevision(idDemande, acteur, corrections)
accepterRevision(idDemande, manager)
abandonner(idDemande, manager)
expirerDemandesDepassees()
```

`DemandeStatutService` est le seul composant autorisé à écrire dans `historique_demande_status`.

Toutes les modifications d'une décision sont transactionnelles. Pour empêcher deux décisions simultanées, l'action reçoit le dernier historique attendu (`statutAttendu`) et verrouille la demande pendant la transition, par exemple avec un verrou JPA pessimiste.

## 7. Adaptation du code existant

- `DemandeService` conserve la création de demande et de détails, mais délègue la création de l'historique initial à `DemandeStatutService`.
- `HistoriqueDemandeStatus` reçoit les champs `etape`, `commentaire` et une date avec heure.
- `UtilisateurService` remplace les vérifications codées en dur sur `MI`, `MC` et `DG` par la vérification du rôle.
- `DemandeController` reste le contrôleur manager et ne contient pas de logique de transition.
- Le repository de vue d'historique est remplacé ou complété par la vue de statut courant, basée sur `MAX(id)`.
- `LoginController` redirige selon le rôle et ajoute une route de déconnexion.

Avant le circuit, fiabiliser aussi la création actuelle :

- envoyer le diplôme sous `detailDemandes[i].diplome.id` dans le formulaire JavaScript ;
- renommer `motif` en `description` partout ;
- vérifier côté serveur que le poste appartient au département choisi ;
- rediriger vers la liste après succès (Post/Redirect/Get) ;
- filtrer la liste manager par périmètre autorisé.

## 8. Écrans et routes

| Module | Route proposée | Rôle |
| --- | --- | --- |
| RH | `/rh/demandes` | demandes créées : valider, refuser, demander correction |
| Manager | `/departements/corrections` | consulter/corriger/abandonner les corrections ouvertes |
| Finance | `/finance/demandes` | demandes validées RH, évaluation budgétaire |
| DG | `/dg/demandes` | demandes validées Finance, validation finale |

Les redirections actuelles vers `/rh/accueil`, `/dg/accueil` et `/accueil` doivent être accompagnées des contrôleurs et templates correspondants.

## 9. Budget

Le budget doit utiliser `BigDecimal`, pas `Double`.

Pour chaque détail :

```text
minimum = nbrPersonnel x budgetMin de la grille
maximum = nbrPersonnel x budgetMax de la grille
```

La grille applicable est la plus récente dont `date_effective <= aujourd'hui`. Le budget global est lu depuis le paramètre `BUDGET`.

```text
maximum <= budget          => OK
minimum <= budget < maximum => correction/revue nécessaire
minimum > budget            => NON
```

Le champ existant `poste.salaire` n'est pas utilisé dans ce calcul.

## 10. Ordre de réalisation

1. Corriger et tester la création actuelle de demande.
2. Ajouter les migrations : rôles, statuts, historique enrichi, révisions, offres.
3. Implémenter `Role`, l'autorisation et `DemandeStatutService`.
4. Implémenter expiration et création d'offres à la validation DG.
5. Implémenter RH, puis la correction manager.
6. Implémenter budget et Finance.
7. Implémenter DG.
8. Ajouter les tests métier et les tests du parcours complet.

## 11. Critères de recette

- Parcours nominal : création -> RH -> Finance -> DG -> offres.
- Refus RH, Finance ou DG.
- Correction demandée par RH, Finance ou DG, suivie d'une correction manager.
- Abandon par le manager.
- Expiration à toutes les étapes non finales.
- Une validation DG ne crée jamais deux fois la même offre.
- Deux actions concurrentes sont bloquées proprement.
- Les transitions interdites sont refusées côté serveur.
- L'historique permet de connaître le statut, l'étape, l'auteur, la date et le commentaire de chaque décision.
