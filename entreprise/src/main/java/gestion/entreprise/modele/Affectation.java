package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affectation")
public class Affectation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_periode_emploi", nullable = false) private PeriodeEmploi periodeEmploi;
    @ManyToOne @JoinColumn(name = "id_poste", nullable = false) private Poste poste;
    @ManyToOne @JoinColumn(name = "id_contrat") private Contrat contrat;
    @Column(name = "date_debut", nullable = false) private LocalDateTime dateDebut;
    @Column(name = "date_fin") private LocalDateTime dateFin;
    @ManyToOne @JoinColumn(name = "id_affectation_statut", nullable = false) private AffectationStatut statut;

    public Affectation() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PeriodeEmploi getPeriodeEmploi() { return periodeEmploi; }
    public void setPeriodeEmploi(PeriodeEmploi periodeEmploi) { this.periodeEmploi = periodeEmploi; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public AffectationStatut getStatut() { return statut; }
    public void setStatut(AffectationStatut statut) { this.statut = statut; }
}