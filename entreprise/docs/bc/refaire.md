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
Voici comment les transactions sont structure. Je veux que tu cree en API REST DemandeController dans dossier api qui permertra de faire ces transactions. Ce controller ne servira que de API REST et pas de redirection de page.
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
- cree fonction Demande_details.getMontant() --> nb effectif * grille de budget montant max pour ce poste = prix du poste demande
- cree fonction Demande.getMontant() --> boucle les demandes details de cette demande et call getMontant(), retourne le prix de la demande
- cree fonction boolean respecteLeBudget(): --> prend le budget dans la table historique_budget_annuel, regarder si demande.getDemande < budget maintenant avec date est lie avec la date de la demande (a respecter les dates)
- sur la card, mettre le montant de la demande (call getMontantOfDemande()). Sur le style, mettre en vert si la demande respecte le truc, sinon en rouge. Call respecteLeBudget(). 
Si manager ET si demande est "a revoir":
- 2 cards: a gauche on a le card originale de la demande originale (ce que le manager a fait), a droite on a le card que le correcteur veut. Puis on a bouton accepter ou abadonner. Si abondonnee, la demadne est completement abondonnee et on ne peut plus rien y faire. Si accepter alors l historique statut de la deamdne prend comme statut "accepter" et comme id employer le correcteur qui a demander. 

pour chaqun: bouton accepte, valider, refuser, a revoir. Pour refuser et a revoir, on aura un modal de confirmation avec input (motif de lhistorique de statut). Tu choisira l'url de controller convenables

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
Respect SOC
url controller en API REST standards.
Dans les 3 layers MVC (a part la base), s'assurer que les donnees sont coherents et logique. Par exemple on ne peut avoir des nb effectif ou prix negatif. OU on ne peut pas avoir une date inferieu a une atre (par exmepl). mettre des check if pour chaque layer avec throw execption et affichage en views.
Pas de overengeneering.
Respecte le schema existatn.
Code le plus simple possible et pro et propre.
ne cree pas de fichier dont on a pas besoin meme si la table existe.
Verify throuhrougly if we handle sessions, roles, generic pages etc


Tu feras tous les layers MVC et meme vue. 