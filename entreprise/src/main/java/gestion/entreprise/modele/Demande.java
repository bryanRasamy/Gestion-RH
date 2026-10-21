package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "matricule", nullable = false, unique = true, length = 50)
    private String matricule;

    @ManyToOne
    @JoinColumn(name = "id_employer", nullable = false)
    private Employer employer;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @OneToMany(mappedBy = "demande")
    private List<HistoriqueDemandeStatus> historiques;

    /*Constructeur*/
    public Demande() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public void setEmployer(Employer employer) {
        this.employer = employer;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }

    public void setHistoriques(List<HistoriqueDemandeStatus> historiques) {
        this.historiques = historiques;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public String getMatricule() {
        return matricule;
    }
    
    public Employer getEmployer() {
        return employer;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }
    
    public List<HistoriqueDemandeStatus> getHistoriques() {
        return historiques;
    }
}
