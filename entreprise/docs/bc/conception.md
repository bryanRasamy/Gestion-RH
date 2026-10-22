# Base
- motif = description. changer column motif en description
- demande_statut: id, libelle, id_employe, date (cree valider, a revoir, refuser, abondonner, expire). id employe est la personne qui a fait l'action. Par exemple, RH a refuser cette demande
- table demande_a_revoir: id, id_demandeur, id_demande_details, date.
- grille_budget: id, id_poste, budget_min, budget_max, date_effective.
- historique_budget_annuel: id, valeur, date_effective
- roles: id, roles (RH, Manager, Finance, DG)
- utilisateur: ajout de colonne role

Transaction:
Liste demande backoffice:
if Date aujourduit > date limite:
statut insert expire
Manager cree demande --> insert historique (cree statut)

RH:
Liste demandes en cards avec statut (historique de demandes qui sont "cree" par le Manager)
Si valider: demande statut = valider RH
si a revoir: insert table demande revoir, statut demande = a revoir
si refuser: demande statut refuser 

Finance:
Liste demandes en cards avec statut (historique de demandes qui sont "valider" de RH)
Voir le montant pour chaque demande (sum des postes dans la demade, nb effectif * prix fourchette budget < BUDGET de cette annee) et voir si validable, a revoir ou non (bouton disabled dynamique). Voir budget 
Si valider: demande statut = valider Finance 
Refuser et a revoir 

DG:
Liste demandes en cards avec statut (historique de demandes qui sont "valider" de Finance)
Si valider: demande statut = valider DG 
si a revoir: insert table demande revoir, statut demande = a revoir
si refuser: demande statut refuser 

Correction Manager:
Show liste demande a revoir.
Details demande:
2 demandes: un ecran a gauche pour ancienne (ce que le MAnager a demander), un ecran a droite avec correction du RH/Finance/DG avec en rouge ce qui a change. ON accete toutes la demande et non un pr un.
Abondonner ou accepter:
Abondoner: statut demande --> abondone
Accepter --> statut demande valider RH/Finance/DG selon qui a "a revoir" cette demande. Donc il faudra tracer qui a a revoir cette demande (avoir une fonctoin qui fait ca), et a partir de ca on peut savoir quel est l'id employe a mettre dans historique statut

Pour chaque ecran avec liste, on a un multifiltre par statut et par date min et max. On peut aussi soit afficer en cards ou en tableau la liste (choix UI). 

ON ne peut rien faire sur les demande expire ou Explicitement refuser. 
On doit ajouter un motif/raison si c'est refuser.

On ajoute `commentaire` (TEXT), `nbr_personnel_propose` (INT, nullable)

Ne cree pas un nouvel sql
motif` (nullable) dans `historique_demande_status`.

BAse toi sur donnee.sql et tables.sql. Efface les codes qui les utilie pas 