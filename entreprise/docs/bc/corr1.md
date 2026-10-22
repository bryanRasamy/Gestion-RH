Continue. Par contre, voici qq remarques que je dois faire avant d allant trop loin:

Ton code est trop specifique. Je m attendait a qqchose de generique pour plus de maintenabilite. Erreur 1: tu cree des fonctions authemtithicateRh,etc...Normalement ca doit jsute etre authentifithaicate uilsieateur et on met des if else a l interieur. Pas besoin de beaucoupe de fonction. ne cree pas DgController, FinanceController, etc,... Ce sera qu un seule controller meme et on redirige vers l acceuil qui le correspon selon le role, une fonction une accetuil avec des if else de role.

Tu t'es aussi trompter dans DemadeRestController: chaque role peut aller dans accepter, et c est a l interieru qui a des if else. Cahque roles peut faire ca. iln y a pas un statut speciique pour un role speicifiqeu. C est tous les roles peuvent faire les differnt statuts et c est id employe qui montre qui a fait l action dans histoique statut.

La solution pour tes probleme en gros: 1 pour n, et non 1 par n. On est generique et non specifique


Deuxiemen chose:
Verifie bien que les filtres resides bien dans le code cote developpeur et non dans le cote client (filtre ). 


Efface tous ces ichiers que j ai dis