package gestion.entreprise.dto;

import java.time.LocalDate;

/**
 * Comparaison d'une ligne de detail : ce que le manager avait demande
 * (colonne de gauche) et la correction proposee par le RH / Finance / DG
 * (colonne de droite).
 */
public class LigneComparaison {

    private final int idDemandeDetails;
    private final String poste;
    private final String departement;
    private final String description;
    private final int nbrPersonnelInitial;
    private final Integer nbrPersonnelPropose;
    private final String commentaire;
    private final String auteur;
    private final String roleAuteur;
    private final LocalDate date;

    public LigneComparaison(int idDemandeDetails, String poste, String departement, String description,
                            int nbrPersonnelInitial, Integer nbrPersonnelPropose, String commentaire,
                            String auteur, String roleAuteur, LocalDate date) {
        this.idDemandeDetails = idDemandeDetails;
        this.poste = poste;
        this.departement = departement;
        this.description = description;
        this.nbrPersonnelInitial = nbrPersonnelInitial;
        this.nbrPersonnelPropose = nbrPersonnelPropose;
        this.commentaire = commentaire;
        this.auteur = auteur;
        this.roleAuteur = roleAuteur;
        this.date = date;
    }

    public int getIdDemandeDetails() { return idDemandeDetails; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDescription() { return description; }
    public int getNbrPersonnelInitial() { return nbrPersonnelInitial; }
    public Integer getNbrPersonnelPropose() { return nbrPersonnelPropose; }
    public String getCommentaire() { return commentaire; }
    public String getAuteur() { return auteur; }
    public String getRoleAuteur() { return roleAuteur; }
    public LocalDate getDate() { return date; }

    /** Vrai lorsque la correction propose un effectif different de l'initial. */
    public boolean estModifie() {
        return nbrPersonnelPropose != null && nbrPersonnelPropose != nbrPersonnelInitial;
    }
}
