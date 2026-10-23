package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposition_embauche_negociation")
public class PropositionEmbaucheNegociation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_proposition_embauche", nullable = false)
    private PropositionEmbauche proposition;

    @Column(name = "remarques_candidat", nullable = false, columnDefinition = "TEXT")
    private String remarquesCandidat;

    @Column(name = "reponse_rh", columnDefinition = "TEXT")
    private String reponseRh;

    @Column(name = "accepte")
    private Boolean accepte;

    @Column(name = "date_negociation", nullable = false)
    private LocalDateTime dateNegociation = LocalDateTime.now();

    public PropositionEmbaucheNegociation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche p) { this.proposition = p; }
    public String getRemarquesCandidat() { return remarquesCandidat; }
    public void setRemarquesCandidat(String r) { this.remarquesCandidat = r; }
    public String getReponseRh() { return reponseRh; }
    public void setReponseRh(String r) { this.reponseRh = r; }
    public Boolean getAccepte() { return accepte; }
    public void setAccepte(Boolean a) { this.accepte = a; }
    public LocalDateTime getDateNegociation() { return dateNegociation; }
    public void setDateNegociation(LocalDateTime d) { this.dateNegociation = d; }
}