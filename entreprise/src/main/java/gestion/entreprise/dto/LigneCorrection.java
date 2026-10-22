package gestion.entreprise.dto;

import java.time.LocalDate;

/** Une ligne de detail faisant l'objet d'une demande de revision. */
public class LigneCorrection {

    private final int idDemandeDetails;
    private final int idPoste;
    private final int idDepartement;
    private final String poste;
    private final String departement;
    private final String description;
    private final int nbrPersonnel;
    private final Integer nbrPersonnelPropose;
    private final String commentaire;
    private final String demandeur;
    private final String roleDemandeur;
    private final LocalDate date;

    public LigneCorrection(int idDemandeDetails, int idPoste, int idDepartement,
                           String poste, String departement, String description,
                           int nbrPersonnel, Integer nbrPersonnelPropose,
                           String commentaire, String demandeur, String roleDemandeur,
                           LocalDate date) {
        this.idDemandeDetails = idDemandeDetails;
        this.idPoste = idPoste;
        this.idDepartement = idDepartement;
        this.poste = poste;
        this.departement = departement;
        this.description = description;
        this.nbrPersonnel = nbrPersonnel;
        this.nbrPersonnelPropose = nbrPersonnelPropose;
        this.commentaire = commentaire;
        this.demandeur = demandeur;
        this.roleDemandeur = roleDemandeur;
        this.date = date;
    }

    public int getIdDemandeDetails() { return idDemandeDetails; }
    public int getIdPoste() { return idPoste; }
    public int getIdDepartement() { return idDepartement; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDescription() { return description; }
    public int getNbrPersonnel() { return nbrPersonnel; }
    public Integer getNbrPersonnelPropose() { return nbrPersonnelPropose; }
    public String getCommentaire() { return commentaire; }
    public String getDemandeur() { return demandeur; }
    public String getRoleDemandeur() { return roleDemandeur; }
    public LocalDate getDate() { return date; }
    public boolean estModifiee() { return nbrPersonnelPropose != null && nbrPersonnelPropose != nbrPersonnel; }
}
