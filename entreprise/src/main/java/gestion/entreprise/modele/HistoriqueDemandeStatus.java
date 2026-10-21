package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

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
    @JoinColumn(name = "id_employer", nullable = false)
    private Employer employer;

    @Column(name = "date", nullable = false)
    private LocalDate date = LocalDate.now();

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

    public void setDate(LocalDate date) {
        this.date = date;
    }

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
    
    public LocalDate getDate() {
        return date;
    }
}
