package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "historique_demande_status")
public class HistoriqueDemandeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_employer")
    private Employer employer;

    @Column(name = "etape", nullable = false, length = 20)
    private String etape;
    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;
    @Column(name = "date_action", nullable = false)
    private OffsetDateTime dateAction = OffsetDateTime.now();

    /*Constructeur*/
    public HistoriqueDemandeStatus() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setDemande(Demande demande) {
        this.demande = demande;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setEtape(String etape) { this.etape = etape; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public void setDateAction(OffsetDateTime dateAction) { this.dateAction = dateAction; }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }

    /*Getters*/
    public int getId() {
        return id;
    }
    
    public Demande getDemande() {
        return demande;
    }

    public Status getStatus() {
        return status;
    }
    
    public Employer getEmployer() {
        return employer;
    }
    
    public String getEtape() { return etape; }
    public String getCommentaire() { return commentaire; }
    public OffsetDateTime getDateAction() { return dateAction; }
}
