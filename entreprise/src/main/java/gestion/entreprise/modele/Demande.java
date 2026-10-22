package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateLimite;

    @OneToMany(mappedBy = "demande")
    private List<HistoriqueDemandeStatus> historiques;

    @OneToMany(mappedBy = "demande")
    private List<DetailDemande> detailDemandes;

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

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public void setHistoriques(List<HistoriqueDemandeStatus> historiques) {
        this.historiques = historiques;
    }

    public void setDetailDemandes(List<DetailDemande> detailDemandes) {
        this.detailDemandes = detailDemandes;
    }

    /*Getters*/
    public int getId() {
        return this.id;
    }

    public String getMatricule() {
        return this.matricule;
    }

    public Employer getEmployer() {
        return this.employer;
    }

    public LocalDateTime getDateLimite() {
        return this.dateLimite;
    }

    public List<HistoriqueDemandeStatus> getHistoriques() {
        return this.historiques;
    }

    public List<DetailDemande> getDetailDemandes() {
        return this.detailDemandes;
    }
}