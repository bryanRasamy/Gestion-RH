package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande_statut")
public class HistoriqueDemandeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_demande", nullable = false)
    private int idDemande;

    @Column(nullable = false, length = 30)
    private String libelle;

    @Column(name = "id_employe")
    private Integer idEmploye;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(nullable = false)
    private LocalDate date;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }

    public Integer getIdEmployer() { return idEmploye; }
    public void setIdEmployer(Integer idEmployer) { this.idEmploye = idEmployer; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}