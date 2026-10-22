package gestion.entreprise.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Ecran de correction : la demande d'origine, l'auteur de la revision et
 * la comparaison ligne a ligne.
 */
public class EcranCorrection {

    private final int idDemande;
    private final String matricule;
    private final LocalDate dateLimite;
    private final String statut;
    private final String auteur;
    private final String roleAuteur;
    private final LocalDate dateRevision;
    private final String commentaire;
    private final List<LigneComparaison> lignes;

    public EcranCorrection(int idDemande, String matricule, LocalDate dateLimite, String statut,
                           String auteur, String roleAuteur, LocalDate dateRevision,
                           String commentaire, List<LigneComparaison> lignes) {
        this.idDemande = idDemande;
        this.matricule = matricule;
        this.dateLimite = dateLimite;
        this.statut = statut;
        this.auteur = auteur;
        this.roleAuteur = roleAuteur;
        this.dateRevision = dateRevision;
        this.commentaire = commentaire;
        this.lignes = lignes;
    }

    public int getIdDemande() { return idDemande; }
    public String getMatricule() { return matricule; }
    public LocalDate getDateLimite() { return dateLimite; }
    public String getStatut() { return statut; }
    public String getAuteur() { return auteur; }
    public String getRoleAuteur() { return roleAuteur; }
    public LocalDate getDateRevision() { return dateRevision; }
    public String getCommentaire() { return commentaire; }
    public List<LigneComparaison> getLignes() { return lignes; }
}
