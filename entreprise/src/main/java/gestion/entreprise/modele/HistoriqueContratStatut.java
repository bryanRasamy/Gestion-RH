package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_contrat_statut")
public class HistoriqueContratStatut {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_contrat", nullable = false) private Contrat contrat;
    @ManyToOne @JoinColumn(name = "id_contrat_statut", nullable = false) private ContratStatut statut;
    @ManyToOne @JoinColumn(name = "id_utilisateur") private Utilisateur utilisateur;

    @Column(name = "motif", columnDefinition = "TEXT") private String motif;
    @Column(name = "date_changement", nullable = false) private LocalDateTime dateChangement = LocalDateTime.now();

    public HistoriqueContratStatut() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Contrat getContrat() { return contrat; }
    public void setContrat(Contrat contrat) { this.contrat = contrat; }
    public ContratStatut getStatut() { return statut; }
    public void setStatut(ContratStatut statut) { this.statut = statut; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDateTime getDateChangement() { return dateChangement; }
    public void setDateChangement(LocalDateTime dateChangement) { this.dateChangement = dateChangement; }
}