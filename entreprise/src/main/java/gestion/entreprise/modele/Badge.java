package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badge")
public class Badge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_employer", nullable = false) private Employer employer;
    @Column(name = "numero", nullable = false, unique = true, length = 50) private String numero;
    @Column(name = "date_emission", nullable = false) private LocalDateTime dateEmission;
    @Column(name = "date_expiration") private LocalDateTime dateExpiration;
    @ManyToOne @JoinColumn(name = "id_badge_statut", nullable = false) private BadgeStatut statut;

    public Badge() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Employer getEmployer() { return employer; }
    public void setEmployer(Employer employer) { this.employer = employer; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDateTime getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDateTime dateEmission) { this.dateEmission = dateEmission; }
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }
    public BadgeStatut getStatut() { return statut; }
    public void setStatut(BadgeStatut statut) { this.statut = statut; }
}