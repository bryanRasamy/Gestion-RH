package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "departement")
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, unique = true, length = 100)
    private String libelle;

    @Column(name = "sigle", nullable = false, unique = true, length = 20)
    private String sigle;

    @OneToMany(mappedBy = "departement")
    private List<Poste> postes;

    @OneToMany(mappedBy = "departement")
    private List<DetailDemande> detailDemandes;

    /*Constructeur*/
    public Departement() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setSigle(String sigle) {
        this.sigle = sigle;
    }

    public void setPostes(List<Poste> postes) {
        this.postes = postes;
    }

    public void setDetailDemandes(List<DetailDemande> detailDemandes) {
        this.detailDemandes = detailDemandes;
    }

    /*Getters*/
    public int getId() {
        return id;
    }
    
    public String getLibelle() {
        return libelle;
    }

    public String getSigle() {
        return sigle;
    }

    public List<Poste> getPostes() {
        return this.postes;
    }

    public List<DetailDemande> getdetailDemandes() {
        return this.detailDemandes;
    }
    
}
