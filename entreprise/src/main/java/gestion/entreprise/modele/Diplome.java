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

    @Column(name = "nom_diplome", nullable = false)
    private int nomDiplome;

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

    public void setNomDiplome(int nomDiplome) {
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
        return this.anneeEtude;
    }

    public int getNomDiplome() {
        return this.nomDiplome;
    }

    public List<DetailDemande> getDetailDemandes() {
        return this.detailDemandes;
    }
}
