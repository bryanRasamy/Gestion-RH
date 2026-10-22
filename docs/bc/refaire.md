On va polish ce qui eiste dans e current codebase. Voici comment tu travaillers: quand je te demande qqchose (modif, ajout, suppr), tu regarderas les dependances de la fonctionnalites dans toutes les lyers MVC (Model, View, Contrller, Servies et BAse) et tu changers toutes les layers avec dependances de ce que je te demanderais. Par exmpel, si dans un models je te demande d'enelver un attribut, teu regardera das les scripts sql la table et enlevera la colonne de la table et vie versa. Ou Par exmeple, si on a qqchose dans une database, on regarde ce qeu ca fais dasn le code et on change. Donc ca par des scripts vers le code.

Voici ce que tu dois faire:

Dans dossier database, enleve les sequences. On n'aura pas de sequences car onutilsiera directement serial

Dans ce query:
UPDATE utilisateur u
SET id_role = r.id
FROM roles r
WHERE u.id_role IS NULL
  AND lower(split_part(u.email, '@', 1)) = lower(r.roles);

Ecris explitement l'email mais ne recherche pas comme ca u.email, '@'. Eris explicaiemten.

Voici ce qu il y auara dasn les tables avec description des tables. Tu cahngers 3 print validation accordingly. tu gardereras mon rthdoatgrphe our les statutes. 

status: c'est les different status d'une demande: creer, valider, refuser, abondonner, expire. Il n y aura que ces status la. Auncun autre 


