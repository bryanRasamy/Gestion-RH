package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "test_technique")
public class TestTechnique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;

    @ManyToOne
    @JoinColumn(name = "id_test_technique_niveau", nullable = false)
    private TestTechniqueNiveau niveau;

    @Column(name = "seuil_reussite", nullable = false)
    private int seuilReussite = 60;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "testTechnique")
    private List<TestTechniqueQuestion> questions;

    public TestTechnique() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Poste getPoste() { return poste; }
    public void setPoste(Poste poste) { this.poste = poste; }
    public TestTechniqueNiveau getNiveau() { return niveau; }
    public void setNiveau(TestTechniqueNiveau niveau) { this.niveau = niveau; }
    public int getSeuilReussite() { return seuilReussite; }
    public void setSeuilReussite(int seuilReussite) { this.seuilReussite = seuilReussite; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public List<TestTechniqueQuestion> getQuestions() { return questions; }
    public void setQuestions(List<TestTechniqueQuestion> questions) { this.questions = questions; }
}