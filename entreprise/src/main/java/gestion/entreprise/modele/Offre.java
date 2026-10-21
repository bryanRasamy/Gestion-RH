package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "offre")
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_detail_demande", nullable = false, unique = true)
    private DetailDemande detailDemande;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    public Offre() {
    }

    public int getId() {
        return id;
    }

    public DetailDemande getDetailDemande() {
        return detailDemande;
    }

    public void setDetailDemande(DetailDemande detailDemande) {
        this.detailDemande = detailDemande;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }
}
