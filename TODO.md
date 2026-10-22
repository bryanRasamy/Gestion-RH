Nb:
    - On utilise ce design a 100% 
    - pour les model , on utilsie int pour les generatedId
    - on utilise bien le oneTomanuy , etc....

- table modifié: 
    - detail_commande
    - candidat
- table ajouté:
    - diplome 

1. [ok] Liste d'offres 

2. [ ] detail offre dans ressources/templates/candidat/detail_offre.html
    * rendre dynamique detail offre , ni le departement, ni date limite et la poste
    - affiche le titre de l'offre
    - affiche le nom de l'entreprise
    - affiche l'annee d'experience demandé

3. [ol] bouton postuler : 
---------------------------------------------------------------- TSY MISY MIOVA 

apres clique sur postuler , on doit voir ces champs:
4. [ ] CV utilisateur pour la candidature de l'offre xxxx dans ressources/templates/candidat/cv.html
    - Prenom
    - Nom
    - email ( pre rempli ( ceci fonctioone deja ))
    - telephone
    - poste visé : ex: xxxx  ( non pas une liste deroulante , prerempli )
    - resume du parcours
    - dernier poste occupe
    - entreprise 
    - diplome : liste deroulante ( liste de diplome de la table diplome )
    - etablissement
    - annee experience professionnnelle : number 

5. Apres creation de candidadure :
    - on garde le nom de cv et lm dans la table candidat 
    - on va stoccker leS fihcier de cv des candidat dans ressources/static/assets/cv
    - on va stoccker leS fihcier de lettres de motivations des candidat dans ressources/static/assets/lm
    NB: les nom de tous les fichiers doit etre differents aux autres 
    on doit gerer ce cas d'une maniere tres facile 

# A faire:
- 