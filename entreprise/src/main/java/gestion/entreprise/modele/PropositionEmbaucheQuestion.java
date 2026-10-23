package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "proposition_embauche_question")
public class PropositionEmbaucheQuestion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_proposition_embauche", nullable = false)
    private PropositionEmbauche proposition;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @ManyToOne @JoinColumn(name = "id_reponse_type")
    private ReponseType reponse;

    @Column(name = "remarque", columnDefinition = "TEXT")
    private String remarque;

    public PropositionEmbaucheQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche p) { this.proposition = p; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int o) { this.ordre = o; }
    public String getQuestion() { return question; }
    public void setQuestion(String q) { this.question = q; }
    public ReponseType getReponse() { return reponse; }
    public void setReponse(ReponseType r) { this.reponse = r; }
    public String getRemarque() { return remarque; }
    public void setRemarque(String r) { this.remarque = r; }
}