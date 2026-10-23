package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "periode_emploi")
public class PeriodeEmploi {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_employer", nullable = false) private Employer employer;
    @Column(name = "date_debut", nullable = false) private LocalDateTime dateDebut;
    @Column(name = "date_fin") private LocalDateTime dateFin;
    @ManyToOne @JoinColumn(name = "id_periode_emploi_statut", nullable = false) private PeriodeEmploiStatut statut;

    public PeriodeEmploi() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Employer getEmployer() { return employer; }
    public void setEmployer(Employer employer) { this.employer = employer; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public PeriodeEmploiStatut getStatut() { return statut; }
    public void setStatut(PeriodeEmploiStatut statut) { this.statut = statut; }
}