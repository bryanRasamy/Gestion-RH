On va polish ce qui eiste dans e current codebase. Voici comment tu travaillers: quand je te demande qqchose (modif, ajout, suppr), tu regarderas les dependances de la fonctionnalites dans toutes les lyers MVC (Model, View, Contrller, Servies et BAse) et tu changers toutes les layers avec dependances de ce que je te demanderais. Par exmpel, si dans un models je te demande d'enelver un attribut, teu regardera das les scripts sql la table et enlevera la colonne de la table et vie versa. Ou Par exmeple, si on a qqchose dans une database, on regarde ce qeu ca fais dasn le code et on change. Donc ca par des scripts vers le code.

Voici ce que tu dois faire:

Database layer (dans database folder):
ajoute tables roles, ajoutes les different roles, dans utilsiateur met id_roles, update les roles de manager, rh, finances et DG avec roles leur id roles repicetinvemt. Je veux des query explicites. et ensuite, Spread this update to the upper layers.

Ajoute ces tables:
- table demande_a_revoir: id, id_demandeur, id_demande_details, nb_souhaiter, id_diplome, nb exp, id_date.
- grille_budget: id, id_poste, budget_min, budget_max, date_effective.
- historique_budget_annuel: id, valeur, date_effective

Met des datas pour chaque. A noter que la table de demande a revoir a forcement une demade details qui et a revoir comme statut. on snapshot direct la demande detail pour fill la ligne directement.

C'est les seul changement dans la base.
Create the Models, Repos of these. Add inside loginCntroller, or any controllers that uses utilisateur, the role of the utilsiateur

Upper Layers:
Voici comment les transactions sont structure. Je veux que tu cree en API REST ValidationController dans dossier api qui permertra de faire ces transactions. Ce controller ne servira que de API REST et pas de redirection de page.
pour les redirection de pages, tu creera des controller pour eux memes. Dans la suite, je definirais seulement les url mais a toi e voir si on redirge ou si c est redierect page:

url: {role.libelle}/acceuil
Va dans l'acceuil de ce role. 
Manager: liste de toutes les demandes QUE CE MANAGER a fait avec leurs status (cet ecran existe deja)
RH: liste de toutes les demandes avec statuts(cree et id_employe=un id d'un manager)
Finance: liste de toutes les demadnes avec statuts(valider et id_eomploye=id du rh)
DG: liste de totes les demadnes avec status(valider et id_employe=id de la Finance)
Tu regardera ces status dans  hsitorique des statuts table.
Tous les acceuil des roles: auront des filtres par status, et  des date min et max
Note que id emplye dasn cette table historiqeu est l'utilisateur qui a fait l'action.

url: {role.libelle}/{statut libelle}
insertion dans la table historiqeu pour pouvoir changer le statut et qui a fait l action
Par exmple si on avais {role.libelle}/valider:
que ce soit rh ou finance ou dg, alors on mettra le status comme valider et l id employer celui de qui a valider. En gros, si on avais rh/valider: c'est le rh qui a valider

Ecrans de voir details demandes:
Si rh ou dg, montre seulement les details.
Si finance: 
- cree fonction getMontantOfPoste(Demande_details) --> nb effectif * grille de budget montant max pour ce poste = prix du poste demande
- cree fonction getMontantOfDemande(Demmande) --> boucle les demandes details de cette demande et call getMontantOfPoste(), retourne le prix de la demande
- cree fonction estDansLeBudget():

- sur la card, mettre le montant de la demande (call getMontantOfDemande()). Sur le style, mettre en vert si la demande est
- 
- avoir une fonction isBudgetSuffisant():
  - regarder la demande 


Ajoute les ecrans de: 
- voir details d'une demande pour rh finance et dg
- voir details 


url: correction/{demande id}


Note que quand un manager cree une demande, le statut est cree,(deja present dans el code existant. Juste pour te rappeler)



Le navbar pour chaques utilisateur est le meme. Met le dans un fragments et link juste dan les views necessaries.







regle:
Pas de dto, on prend explicitement les attributs
Pas de constante.
Pas de hardcoding dans le code.
Par exmple pour get un statut et compare on ne va pas faire "cree".equals() mais on va faire statusrepostiory.getCreer().Equals()...Idem pour les Roles
Convention en anglias, SpringBoot et API REST.
Simplicite au maximum. 
N utilise jamais BigDecimal, fait plutot Double ou Integer avec numeric() sur la colonne par exmpel.
N utilsie jamais Optional.
N utilise jamasi de stream ou de colletions masi fais des codes explicite avec for et if/
respecte le style.css. Ajoute des classe de style si tu veux.