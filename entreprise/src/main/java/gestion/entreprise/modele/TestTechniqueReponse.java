package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "test_technique_reponse")
public class TestTechniqueReponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_test_technique_passage", nullable = false)
    private TestTechniquePassage passage;

    @ManyToOne
    @JoinColumn(name = "id_test_technique_question", nullable = false)
    private TestTechniqueQuestion question;

    @Column(name = "reponse_choisie", nullable = false, length = 1)
    private String reponseChoisie;

    @Column(name = "correcte", nullable = false)
    private boolean correcte;

    public TestTechniqueReponse() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public TestTechniquePassage getPassage() { return passage; }
    public void setPassage(TestTechniquePassage passage) { this.passage = passage; }
    public TestTechniqueQuestion getQuestion() { return question; }
    public void setQuestion(TestTechniqueQuestion question) { this.question = question; }
    public String getReponseChoisie() { return reponseChoisie; }
    public void setReponseChoisie(String reponseChoisie) { this.reponseChoisie = reponseChoisie; }
    public boolean isCorrecte() { return correcte; }
    public void setCorrecte(boolean correcte) { this.correcte = correcte; }
}