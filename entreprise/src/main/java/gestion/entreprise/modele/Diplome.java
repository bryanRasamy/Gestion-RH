package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "diplome")
public class Diplome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "annee_etude", nullable = false)
    private int anneeEtude;

    @Column(name = "nom_diplome", nullable = false, length = 100)
    private String nomDiplome;

    @OneToMany(mappedBy = "diplome")
    private List<DetailDemande> detailDemandes;

    /*Constructeur*/
    public Diplome() {
    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setAnneeEtude(int anneeEtude) {
        this.anneeEtude = anneeEtude;
    }

    public void setNomDiplome(String nomDiplome) {
        this.nomDiplome = nomDiplome;
    }

    public void setDetailDemandes(List<DetailDemande> detailDemandes) {
        this.detailDemandes = detailDemandes;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public int getAnneeEtude() {
        return anneeEtude;
    }

    public String getNomDiplome() {
        return nomDiplome;
    }

    public List<DetailDemande> getDetailDemandes() {
        return detailDemandes;
    }
}