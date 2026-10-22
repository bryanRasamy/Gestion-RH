package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "test_technique_question")
public class TestTechniqueQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_test_technique", nullable = false)
    private TestTechnique testTechnique;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    @Column(name = "enonce", nullable = false, columnDefinition = "TEXT")
    private String enonce;

    @Column(name = "choix_a", nullable = false, columnDefinition = "TEXT")
    private String choixA;

    @Column(name = "choix_b", nullable = false, columnDefinition = "TEXT")
    private String choixB;

    @Column(name = "choix_c", nullable = false, columnDefinition = "TEXT")
    private String choixC;

    @Column(name = "choix_d", nullable = false, columnDefinition = "TEXT")
    private String choixD;

    @Column(name = "reponse_correcte", nullable = false, length = 1)
    private String reponseCorrecte;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public TestTechniqueQuestion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public TestTechnique getTestTechnique() { return testTechnique; }
    public void setTestTechnique(TestTechnique testTechnique) { this.testTechnique = testTechnique; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public String getEnonce() { return enonce; }
    public void setEnonce(String enonce) { this.enonce = enonce; }
    public String getChoixA() { return choixA; }
    public void setChoixA(String choixA) { this.choixA = choixA; }
    public String getChoixB() { return choixB; }
    public void setChoixB(String choixB) { this.choixB = choixB; }
    public String getChoixC() { return choixC; }
    public void setChoixC(String choixC) { this.choixC = choixC; }
    public String getChoixD() { return choixD; }
    public void setChoixD(String choixD) { this.choixD = choixD; }
    public String getReponseCorrecte() { return reponseCorrecte; }
    public void setReponseCorrecte(String reponseCorrecte) { this.reponseCorrecte = reponseCorrecte; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}