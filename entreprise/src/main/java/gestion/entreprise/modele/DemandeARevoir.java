package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande_a_revoir")
public class DemandeARevoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Employer demandeur;

    @ManyToOne
    @JoinColumn(name = "id_demande_details", nullable = false)
    private DetailDemande demandeDetails;

    @Column(name = "nb_souhaite", nullable = false)
    private int nbSouhaite;

    @Column(name = "id_date", nullable = false)
    private LocalDateTime idDate = LocalDateTime.now();

    /*Constructeur*/
    public DemandeARevoir() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setDemandeur(Employer demandeur) {
        this.demandeur = demandeur;
    }

    public void setDemandeDetails(DetailDemande demandeDetails) {
        this.demandeDetails = demandeDetails;
    }

    public void setNbSouhaite(int nbSouhaite) {
        this.nbSouhaite = nbSouhaite;
    }

    public void setIdDate(LocalDateTime idDate) {
        this.idDate = idDate;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public Employer getDemandeur() {
        return demandeur;
    }

    public DetailDemande getDemandeDetails() {
        return demandeDetails;
    }

    public int getNbSouhaite() {
        return nbSouhaite;
    }

    public LocalDateTime getIdDate() {
        return idDate;
    }
}
