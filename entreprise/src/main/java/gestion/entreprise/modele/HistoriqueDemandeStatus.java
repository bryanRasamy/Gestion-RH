package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Historique des actions realisees sur une demande : une ligne par changement
 * de statut.
 *
 * <p>{@code idEmploye} identifie la personne qui a realise l'action (la
 * connexion {@code id_employer} est nullable : l'expiration est une action du
 * systeme). {@code motif} porte la raison du refus ou le commentaire de la
 * revision.</p>
 */
@Entity
@Table(name = "historique_demande_status")
public class HistoriqueDemandeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_demande", nullable = false)
    private int idDemande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    @Column(name = "id_employer")
    private Integer idEmploye;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(nullable = false)
    private LocalDate date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /** Libelle du statut (table {@code status}). */
    public String getLibelle() {
        return status != null ? status.getLibelle() : null;
    }

    public Integer getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(Integer idEmploye) {
        this.idEmploye = idEmploye;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
