package gestion.entreprise.dto;

/** Ligne de detail d'une demande, enrichie des libelles poste / departement. */
public class LigneDemande {

    private final int id;
    private final String poste;
    private final String departement;
    private final String description;
    private final int nbrPersonnel;
    private final String diplome;
    private final int anneeExperience;

    public LigneDemande(int id, String poste, String departement, String description,
                        int nbrPersonnel, String diplome, int anneeExperience) {
        this.id = id;
        this.poste = poste;
        this.departement = departement;
        this.description = description;
        this.nbrPersonnel = nbrPersonnel;
        this.diplome = diplome;
        this.anneeExperience = anneeExperience;
    }

    public int getId() { return id; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDescription() { return description; }
    public int getNbrPersonnel() { return nbrPersonnel; }
    public String getDiplome() { return diplome; }
    public int getAnneeExperience() { return anneeExperience; }
}
