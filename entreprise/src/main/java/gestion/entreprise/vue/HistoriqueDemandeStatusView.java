package gestion.entreprise.vue;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "v_historique_demande_status")
public class HistoriqueDemandeStatusView {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "id_demande")
    private int idDemande;

    @Column(name = "id_status")
    private int idStatus;

    @Column(name = "id_employer_evaluateur")
    private int idEmployerEvaluateur;

    @Column(name = "nom_evaluateur")
    private String nomEvaluateur;

    @Column(name = "prenom_evaluateur")
    private String prenomEvaluateur;

    @Column(name = "matricule")
    private String matricule;

    @Column(name = "id_employer")
    private int idEmployer;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "sigle")
    private String sigle;

    @Column(name = "motif")
    private String motif;

    @Column(name = "date")
    private LocalDateTime date;

    /**
     * Champ calculé côté service (non mappé en base).
     * Contient le sigle du rôle de l'évaluateur : "RH", "FINANCE", "DG", "MANAGER"…
     * Utilisé pour construire le libellé "Validé par RH / Finance / DG".
     */
    @Transient
    private String evaluateurRole;

    /**
     * Libellé enrichi affiché dans le pill de statut.
     * Calculé côté getter, donc aucun impact base.
     *
     * Exemples :
     *   sigle = "VA", evaluateurRole = "RH"      → "Validé par RH"
     *   sigle = "VA", evaluateurRole = "FINANCE" → "Validé par Finance"
     *   sigle = "VA", evaluateurRole = "DG"      → "Validé par DG"
     *   sigle = "CR"                             → "Créée"
     *   sigle = "RF", evaluateurRole = "RH"      → "Refusé par RH"
     *   sigle = "AR", evaluateurRole = "FINANCE" → "À revoir par Finance"
     *   sigle = "AN"                             → "Annulée"
     */
    public String getLibelleEnrichi() {
        if (sigle == null) return libelle;
        String role = (evaluateurRole != null) ? formatRole(evaluateurRole) : "";
        switch (sigle) {
            case "VA":
                return role.isEmpty() ? "Validé" : "Validé par " + role;
            case "RF":
                return role.isEmpty() ? "Refusé" : "Refusé par " + role;
            case "AR":
                return role.isEmpty() ? "À revoir" : "À revoir par " + role;
            default:
                return libelle;
        }
    }

    private static String formatRole(String sigleRole) {
        if (sigleRole == null) return "";
        switch (sigleRole.toUpperCase()) {
            case "RH":      return "RH";
            case "FINANCE": return "Finance";
            case "DG":      return "DG";
            case "MANAGER": return "Manager";
            default:        return sigleRole;
        }
    }

    /*Constructeur*/
    public HistoriqueDemandeStatusView() {
    }

    /*Setters*/
    public void setId(int id) { this.id = id; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }
    public void setIdStatus(int idStatus) { this.idStatus = idStatus; }
    public void setIdEmployerEvaluateur(int idEmployerEvaluateur) { this.idEmployerEvaluateur = idEmployerEvaluateur; }
    public void setNomEvaluateur(String nomEvaluateur) { this.nomEvaluateur = nomEvaluateur; }
    public void setPrenomEvaluateur(String prenomEvaluateur) { this.prenomEvaluateur = prenomEvaluateur; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public void setIdEmployer(int idEmployer) { this.idEmployer = idEmployer; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public void setSigle(String sigle) { this.sigle = sigle; }
    public void setMotif(String motif) { this.motif = motif; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public void setEvaluateurRole(String evaluateurRole) { this.evaluateurRole = evaluateurRole; }

    /*Getters*/
    public int getId() { return id; }
    public int getIdDemande() { return idDemande; }
    public int getIdStatus() { return idStatus; }
    public int getIdEmployerEvaluateur() { return idEmployerEvaluateur; }
    public String getNomEvaluateur() { return nomEvaluateur; }
    public String getPrenomEvaluateur() { return prenomEvaluateur; }
    public String getMatricule() { return matricule; }
    public int getIdEmployer() { return idEmployer; }
    public String getLibelle() { return libelle; }
    public String getSigle() { return sigle; }
    public String getMotif() { return motif; }
    public LocalDateTime getDate() { return date; }
    public String getEvaluateurRole() { return evaluateurRole; }
}