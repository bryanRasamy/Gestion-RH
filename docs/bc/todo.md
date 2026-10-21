# TODO Sprint : circuit de validation des demandes

Équipe : **Personne A** et **Personne B**. Existant : la création d'une demande par le manager. Objectif : RH → Finance → DG → création des demandes, avec révision et correction par le manager.

## 1. Circuit et statuts

Statuts (table `status`) : créé, validé RH, validé Finance, validé DG, à revoir, refusé, abandonné, expiré.

| Depuis | Transitions autorisées |
| --- | --- |
| créé | validé RH, à revoir, refusé, expiré |
| validé RH | validé Finance, à revoir, refusé, expiré |
| validé Finance | validé DG, à revoir, refusé, expiré |
| à revoir | validé RH, validé Finance ou validé DG (selon qui a demandé la révision), abandonné, expiré |
| validé DG, refusé, abandonné, expiré | aucune (états finaux) |

Le statut courant d'une demande est sa dernière ligne dans `historique_demande_status` (plus grand `id`, mais on prenant toujours en compte la `date` ).

## 2. Répartition

|  | Personne A | Personne B |
| --- | --- | --- |
| Thème | Fondation, RH, correction manager | Budget, Finance, DG, recette |
| SQL | `02_demande_revision_demande.sql` | `03_budget.sql`, `99_donnees_test.sql` |
| Services | statut, révision, demande, expiration | budget |
| Écrans | RH, correction manager | composant carte, Finance, DG |

## 3. Hypothèses à confirmer avant de coder

1. **\[Bloquant\] `demande_a_revoir`** : les colonnes de la spec ne permettent pas d'afficher la correction du RH/DG. On ajoute `commentaire` (TEXT), `nbr_personnel_propose` (INT, nullable) et `traite` (BOOLEAN, défaut faux). `id_mpangataka` = l'employé (RH, Finance ou DG) qui demande la révision. Une ligne par ligne de détail concernée.
2. **\[Bloquant\] Rôles** : rien dans les tables actuelles ne dit qui est RH, Finance, DG ou manager. À décider : colonne `role` sur `utilisateur`, ou déduction par poste/département. A vérifie d'abord ce que le code existant fait déjà.
3. **Statut** : la table `status` (id, libelle) contient les 8 valeurs. L'employé et la date sont dans `historique_demande_status` (nouvelle colonne `id_employer`, nullable car l'expiration est faite par le système). On garde les noms existants : `employer`, `status`, `detail_demande`.
4. **Budget** : `parametre` code BUDGET = budget global. Montant d'une demande = somme de (nbr\_personnel × budget de la grille du poste), en min et en max, avec la grille la plus récente dont `date_effective` ≤ aujourd'hui. Une demande est validable si le montant max ≤ BUDGET. Pas de déduction des demandes déjà validées (sinon : budget restant = BUDGET − demandes validées Finance/DG). On ignore `poste.salaire` pour ce sprint.
5. **Acceptation d'une correction** : la demande prend le statut de l'étape qui avait demandé la révision (RH → validé RH, Finance → validé Finance, DG → validé DG, avec création des demandes). Cette étape se déduit du statut précédent dans l'historique : créé → RH, validé RH → Finance, validé Finance → DG. La spec ne cite que RH et DG : on applique la même règle à la Finance.
6. **Expiration** : concerne les demandes créé, validé RH, validé Finance ou à revoir dont `date_limite` < aujourd'hui. Une demande validé DG n'expire pas (l'demande a sa propre date limite).
7. **demande** : une ligne par `detail_demande` à la validation DG, `date_limite` copiée de la demande, une seule fois (UNIQUE sur `id_detail_demande`).
8. **Motif de refus** : pas de colonne prévue. Proposition : `motif` (nullable) dans `historique_demande_status`.

## 4. Règles anti-conflit

- Chacun ne modifie que ses fichiers. Besoin de toucher un fichier de l'autre : on demande.
- **A est propriétaire** de : code de création de demande existant, menu/navigation, configuration de sécurité, entités `Demande`, `DetailDemande`, `HistoriqueDemandeStatus`, `DemandeARevoir`, `demande`, services statut/révision, packages `rh` et `correction`. A ajoute dès A0.8 toutes les entrées de menu (RH, Finance, DG, Correction) avec des routes vides : B ne touche jamais au menu.
- **B est propriétaire** de : `GrilleBudget`, `Parametre`, `BudgetService`, composant carte, packages `finance` et `dg`, données de test.
- Scripts SQL : un fichier numéroté par personne, jamais d'édition du fichier de l'autre.
- Un package et un dossier de templates par module.
- Branches `feature/a-<tâche>` et `feature/b-<tâche>`, PR courtes relues par l'autre, `git pull --rebase` sur main chaque matin.
- Aucun écran n'écrit directement dans `historique_demande_status` : tout passe par les services ci-dessous.

## 5. Contrats d'interface

À figer ensemble avant de commencer (signatures indicatives).

**Fournis par A**

- `StatutDemande` : enum des 8 libellés (source unique, B l'importe).
- `DemandeStatutService.statutCourant(idDemande)`.
- `DemandeStatutService.listerParStatut(statut)` : appelle d'abord l'expiration, puis renvoie les demandes dont le statut courant est celui demandé.
- `DemandeStatutService.changer(idDemande, nouveauStatut, idEmployer)` : une transaction, vérifie la transition, insère l'historique. Si le statut est validé DG, crée les demandes.
- `DemandeStatutService.expirerDemandesDepassees()`.
- `DemandeARevoirService.demanderRevision(idDemande, idEmployer, lignes)` avec lignes = (idDetailDemande, commentaire, nbrPersonnelPropose) : insère les lignes et passe la demande à à revoir, en une transaction.
- Vue SQL `v_demande_statut_courant`.

**Fournis par B**

- `BudgetService.montantDemande(idDemande)` → min et max.
- `BudgetService.budgetDisponible()`.
- `BudgetService.evaluer(idDemande)` → OK, A\_REVOIR ou NON.
- Fragment de template `carte-demande`.

## 6. Phase 0 : fondations (A et B en parallèle)

Tailles : S = petit, M = moyen, L = gros.

### Personne A

- [ ] **A0.1 (M)** Script `02_demande_revision_demande.sql` : renommer `detail_demande.motif` en `description` ; ajouter `historique_demande_status.id_employer` (FK vers `employer`, nullable) ; créer `demande_a_revoir` (id, id\_mpangataka, id\_detail\_demande, date, commentaire, nbr\_personnel\_propose, traite) ; créer `demande` (id, id\_detail\_demande unique, date\_limite) ; insérer les 8 statuts ; créer la vue `v_demande_statut_courant`.
- [ ] **A0.2 (S)** Répercuter le renommage motif → description dans le code existant (entité, formulaire, templates, requêtes) et vérifier que la création de demande marche toujours.
- [ ] **A0.3 (S)** Entités et repositories : `DemandeARevoir`, `demande`, `idEmployer` dans `HistoriqueDemandeStatus`.
- [ ] **A0.4 (M)** `StatutDemande` et `DemandeStatutService` (statut courant, liste par statut, changement avec contrôle des transitions, création des demandes au validé DG).
- [ ] **A0.5 (S)** `expirerDemandesDepassees()`.
- [ ] **A0.6 (S)** `DemandeARevoirService.demanderRevision`.
- [ ] **A0.7 (S)** Création de demande : insérer le statut créé avec l'`id_employer` du manager, dans la même transaction.
- [ ] **A0.8 (S)** Menu : entrées RH, Finance, DG, Correction avec routes vides et accès par rôle (dépend de l'hypothèse 2).
- [ ] **A0.9 (M)** Tests : transitions autorisées et interdites, expiration, demandes (une par détail, date limite copiée, pas de doublon), révision.

**Jalon M0** : A0.1 à A0.8 mergés sur main. À faire en premier, vite.

### Personne B

- [ ] **B0.1 (S)** Script `03_budget.sql` : `grille_budget` (id, id\_poste, budget\_min, budget\_max, date\_effective, CHECK min ≤ max) et `parametre` (id, code, valeur, date\_effective) avec la ligne BUDGET. Jamais d'UPDATE : un changement = une nouvelle ligne avec une nouvelle `date_effective`.
- [ ] **B0.2 (S)** Entités et repositories `GrilleBudget` et `Parametre`.
- [ ] **B0.3 (M)** `BudgetService`. Règle d'évaluation proposée : OK si max ≤ budget ; A\_REVOIR si min ≤ budget < max ; NON si min > budget.
- [ ] **B0.4 (M)** Fragment `carte-demande` réutilisable : statut courant en badge, date limite, liste des postes (poste, département, nbr\_personnel, description), zone montants, zone boutons d'action.
- [ ] **B0.5 (S)** Script `99_donnees_test.sql` : un employé par rôle, postes avec grilles, une demande à chaque statut, une demande avec date limite dépassée.
- [ ] **B0.6 (S)** Tests de `BudgetService` : grille la plus récente, plusieurs postes, budget insuffisant.

B0.1 à B0.3 et B0.5 ne dépendent pas de A. B0.4 et la suite attendent M0.

## 7. Phase 1 : écrans (A et B en parallèle, après M0)

### Personne A : RH (M)

- [ ] **A1.1** Liste en cartes des demandes dont le statut courant est créé.
- [ ] **A1.2** Valider (modal de confirmation) → `changer(validé RH)`.
- [ ] **A1.3** À revoir : modal avec commentaire par ligne et nombre proposé facultatif → `demanderRevision`.
- [ ] **A1.4** Refuser : modal de confirmation → `changer(refusé)`.
- [ ] **A1.5** Accès réservé au rôle RH ; message clair si la demande a changé de statut entre-temps.

### Personne A : correction par le manager (L)

- [ ] **A2.1** Liste des demandes à revoir du manager connecté.
- [ ] **A2.2** Écran détail en deux colonnes : ancienne demande et correction (auteur avec son rôle, date, commentaire, valeur proposée), lignes non traitées uniquement.
- [ ] **A2.3** Abandonner : modal → `changer(abandonné)`.
- [ ] **A2.4** Accepter : modal ; appliquer les valeurs proposées aux `detail_demande` ; marquer les lignes `traite` ; déduire le statut cible du statut précédent ; `changer(...)`.
- [ ] **A2.5** Cas des révisions successives (RH puis DG sur la même demande).
- [ ] **A2.6** Tests du parcours complet.

### Personne B : Finance (M)

- [ ] **B1.1** Liste en cartes des demandes dont le statut courant est validé RH.
- [ ] **B1.2** Affichage du montant min et max, du budget et de l'indicateur (OK, A\_REVOIR, NON).
- [ ] **B1.3** Valider → `changer(validé Finance)`, avec modal de confirmation renforcée si le budget est dépassé.
- [ ] **B1.4** À revoir (`demanderRevision`) et refuser, avec modals.
- [ ] **B1.5** Accès réservé au rôle Finance ; gestion du changement de statut concurrent.

### Personne B : DG (S à M)

- [ ] **B2.1** Liste en cartes des demandes dont le statut courant est validé Finance.
- [ ] **B2.2** Valider → `changer(validé DG)` ; afficher le nombre d'demandes créées.
- [ ] **B2.3** À revoir et refuser, avec modals.
- [ ] **B2.4** Accès réservé au rôle DG.

## 8. Phase 2 : recette (ensemble)

Chacun teste les écrans de l'autre.

| # | Scénario | Testeur |
| --- | --- | --- |
| 1 | Parcours nominal : créé → RH → Finance → DG, demandes créées avec la date limite | B |
| 2 | Refus par la RH | B |
| 3 | RH à revoir → manager accepte → validé RH → visible chez la Finance | B |
| 4 | RH à revoir → manager abandonne | B |
| 5 | Finance à revoir (budget) → manager corrige → validé Finance → visible chez le DG | A |
| 6 | DG à revoir → manager accepte → validé DG et demandes créées | A |
| 7 | DG refuse | A |
| 8 | Expiration à chaque étape ; une demande validé DG n'expire pas | A et B |
| 9 | Deux actions simultanées sur la même demande | A et B |

## 9. Terminé quand

- Les 9 scénarios passent.
- Aucune écriture directe dans l'historique en dehors des services.
- Les transitions interdites sont refusées côté serveur (pas seulement masquées dans l'interface).
- Chaque action importante passe par un modal de confirmation.
- Les scripts SQL se rejouent à partir d'une base vide.
