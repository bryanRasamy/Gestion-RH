valider and accepter is the same thing ! Keep only valider.
le statut valider et accepter sont la meme chose. Corrige cote base dans les scripts. ON gargera de valider. Corrige les upperlayers (MVC)

Il y a une incoherence et un bug de worklow de statut:
je vais dans manager, je vois different demandes avec differnetse satuts. 
je vais dans rh, et je vois que toutes ces meme demandes qui etaient dans manager ont tous les statut "cree" affiche sur eux. 

Aussi je crois que ce bug peut cree une incoherence de workflwo:
le RH ne voit que LES DEMANDE AVEC STATUS ACTUELLE CREE, PAR LE MANAGER
La Finance ne voit que LES DEMANDES AVEC STATSU ACTUELLE VALIDER, PAR LE RH
le DG ne voit que LES DEMANDES AVEC STATUS ACTUALLE VALIDER, PAR LA FINANCE.
Rien d'autres que ca. Revoit le worklwof, l'affihage et les autres fichiers relatives.

Donc on regarde en meme temps dans l'historique id_status et id_employer.

il n y a pas de salaire dans poste, c est la grille qui permet de savoir le salaire d'un poste (au maximum). Corrige la table et les ficheirs relatvies dans les layers plus haut.

Foncitonnalite qui te demandera de la concentration:
LEs boutons du manager dans details de demande ne sont que abondonner 
quand on clique sur a revoir (pour n'importe quel roles), on va dans une page qui est exatement simliarie a un update demande: donc c est juste un copie colel de la page de nouvelle demadne, mais avec valeur prerempli. et c est  quand on valide cette demande de corrrectoin que on insert dans la table de correcton, comme un snapshot pour a revoir. Les seuls choses qu un correcteur peut corriger est: l'effectif. Le reste c'est a jete. Donc disable els autres champs
Verifie aussi cote layer serveur que on ne peut pas faire rentrer d'autres champs a part l'effectif.

Dans l'acceuil de la finance, ajoute le budget annuel actuelle de l'entreprise

Verifie que on ne peut pas faire rentrer de valeur negatives pour les effectifs Guards cote serveur et client, comme d habitude