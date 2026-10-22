package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_technique_passage")
public class TestTechniquePassage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne
    @JoinColumn(name = "id_test_technique", nullable = false)
    private TestTechnique testTechnique;

    @Column(name = "score")
    private Integer score;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut = "EN_COURS";

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut = LocalDateTime.now();

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    /**
     * Les réponses données par le candidat.
     * Cascade ALL : le save(passage) persiste toutes les réponses d'un coup.
     * orphanRemoval : retirer une réponse de la liste la supprime en base.
     */
    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestTechniqueReponse> reponses = new ArrayList<>();

    public TestTechniquePassage() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }
    public TestTechnique getTestTechnique() { return testTechnique; }
    public void setTestTechnique(TestTechnique testTechnique) { this.testTechnique = testTechnique; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public List<TestTechniqueReponse> getReponses() { return reponses; }
    public void setReponses(List<TestTechniqueReponse> reponses) { this.reponses = reponses; }

    /** Ajoute une réponse en garantissant le lien bidirectionnel. */
    public void addReponse(TestTechniqueReponse reponse) {
        reponses.add(reponse);
        reponse.setPassage(this);
    }
}