package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_proposition_embauche_statut")
public class HistoriquePropositionEmbaucheStatut {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_proposition_embauche", nullable = false)
    private PropositionEmbauche proposition;

    @ManyToOne @JoinColumn(name = "id_proposition_embauche_statut", nullable = false)
    private PropositionEmbaucheStatut statut;

    @ManyToOne @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement = LocalDateTime.now();

    public HistoriquePropositionEmbaucheStatut() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche p) { this.proposition = p; }
    public PropositionEmbaucheStatut getStatut() { return statut; }
    public void setStatut(PropositionEmbaucheStatut s) { this.statut = s; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur u) { this.utilisateur = u; }
    public String getMotif() { return motif; }
    public void setMotif(String m) { this.motif = m; }
    public LocalDateTime getDateChangement() { return dateChangement; }
    public void setDateChangement(LocalDateTime d) { this.dateChangement = d; }
}