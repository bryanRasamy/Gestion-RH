Application de gestion RH.

Sprint 1: Embaucher

1. Expression du besoin d'un manager de departement:
Le manager veut creer ou remplacer un poste
informations dont on a besoin: poste, departement, effectif, motif, budget, date souhaite
Pour le budget il faut passer par la RH et la finance pour avoir une grille de budget pour un poste pour l'entreprise. La grille depend de l'entreprise
Donc le budget est determines selon: la grille du poste * l'effectif.
Ensuite lors de la validation du formulaire, on passe par une validation du departmenet de la finance: est ce que on est OKAY pour ce budget demande ?
Donc le workflow est:
Manager du departement envoi form --> RH dois valider cette demande --> check vers la Finance pour pouvoir valider si le budget est okay --> la direction generale aussi doit valider --> ouverture du recrutement.
Objets metier: Departement, Poste, DemandeRecrutement, Validation

2. Recrutement:
Des candidats se connectent sur la platforme et postule pour le poste ouvert. ON doit pouvoir gerer plusieur candidatures. Il a 2 moyen de faire son CV (il doit au moins faire une des deux, mais ce n est pas oblige qu il fasse l un s il a deja fait l'autre): CV a uploader (pdf,...) et CV a remplir par l'interface (pour guider le candidat: experience, education,...). Specifier la civilite par exmple: Ms, Mr,..
POur la selection il y a plusieurs mode:
D'abord on fait un test technique. Le test technique a 3 niveaux ("Test ecrit", "Niveau 2", "Niveau 3"). Puis on passe a l'entretien (stocker qui a fait l'entretien).
Apres validation de test technque et entretien, le candidat est retenu\
Objets: OffreEmploi, Candidat, Candidature, Entretien, EvaluationCandidat

3. Selection et Embauche
Le candidat retenu recois une proposition d'embauche. Il recoit un form qu'il doit remplir, ou il y aura certaines quetsion qu il doit repondre par oui/non/autres remarques. Par emple dans ce form il y aura: "Votre salaire sera de XXXX $. Acceptez vous ?/ Date et heure de travail, Lieu de travail.".
Si autres il y aura un champ remarque ou il doit dire c est quoi son avis.
A la fin, il y aura 3 boutons: Accepter l'offre, Attendre, Refuser. Si toutes les questions ont ete repondu par "oui" alors les 3 boutons sont enabled, sinon, accepter l'offre sera disabled et les 2 autres seront enabled. AJout de modal de vailidation avec remarqeus aussi par exmemple. Si il clique attendre --> la demande revient a l'entreprise -->  l'entreprise valide les remaqures/demande du candidat --> la demande revient au candidat --> Le candidat peut accepter ou refuser.
Si il clique accepte --> on creer et genere automatiquement un pdf du  contrat de travail: poste et affectation, les questions du questionaires,...

4. Onboading et contract
Lors de la creation du contat, le futur salarie doit choisir entre un CE, CDD ou CDI. Par defaut, il aura un CE. 
CE: 6 mois renouvelable 1 fois (maximum). Chaque entreprise aura sa speicifte de contrat d'essai
CDD: pas oblige de faire apes CE, c'est a dire apres CE, le candidat peut directement aller en CDI. CDD est 2 ans max renouvelable 1 fois. 
CDI:
Tracabilites des mmouvements de chaque contrat pour un candidat: quand est fin, quand est debut. etc,...
Ces regles seront nos regles pour l'application.
Etat du contrat: cree --> signe par le candidat --> valide par la direction.
Si tous ces etats ont ete fait, alors le candidat aura un badge + num immatricuel et entrera dans l'etat de paie periodique, c est a dire la liste des personne qui recoie une paie. 
Un candidats peut avoir plusieur contrats successifs: CDD, renouvelement, CDi,..
Autres specifiation sur ce bloc 4:
Ne pas ecraser l'historique en stockant seuelmt employe.type_contrat
Onboarding recu: contrat signe, badge, compte bancaire (facultatif, la RH s'en occupe pour ouvrir le compte banquaire --> voici ton numero de compte), materiel (ordi, voiture, secretaire), formation (ex: avoir une formation d'assurance pendant 2 mois, avant de pouvoir avoir ce poste), inscription dasn l'etat de paie periodique.. 
L'entreprise doit avoir une specificite: les paie doit avoir des modes (virement bancaire etc,...)
Utilsier une checklist et un workflow d'integration
Objets: Contrat, Affectation, Onboarding, OnboardingTache, Document

A faire/chercher:
dans le bloc2 (recrutement):
- il faudra chercher/cree different test technique pour simuler la candidature. 
- creer un formulaire de CV si CV a remplir
dans le bloc3 (selection et embauche):
- c'est comment un contrat de travail ? c'est comment la forme etc...
dans le bloc4 (contrat et onboarding):
- c'est quoi les caractersituqes de chaque conrat (CDD, CE, CDI) avec renouvelabilite et frequence de renouvelabiilite. Les vrai caracteristiques de notre application est specifie en haut
- c est quoi les autre elements a avoir dans le onboarding ?

Point a respecter:
- Application hautement tracable: historique toujours.
- Un poste est different d'un employe
- un candidat est different d'un employe, une personne peut avoir plusieurs candidateurs avant de devenir salaries.

Doit avoir:
- On rajoutera toujours des modals de validation avant submit qqchose d'imporatnat
- pagination partout, surtout pour les forms longs comme les questionnaires.
- authentification
- notifications
- historiques / logs / audits

Extra:
- download pdf de fiche de poste d'embauche
- download excel de liste d'emploi,...
- statistiques dashboard

Stack: 
En Dev:
SpringBoot + Thymleaf/JSP (a debattre) + Postgres
Architecture: MVC, privilegie API REST avec separation des Controllers de Page et les Controller API.
GED / stockage de documents.

En Prod: 
Deployement sur Render + Supabase. Application mobile.

Les objets metiers ne sont pas oblige d'etre suivi.

## Connaissance:
Structure d'une entreprise:
Grandes entreprise --> Direction existante
Moyene entreprise --> Departement
Petite entreprse OU Petite et Moyenne Entreprise --> Services
Tres petites entreprise --> rien: gerant et direct travail