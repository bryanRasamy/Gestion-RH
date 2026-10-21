package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "poste")
public class Poste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "sigle", nullable = false, length = 20)
    private String sigle;

    @Column(name = "salaire", nullable = false)
    private Double salaire;

    /*Constructeur*/
    public Poste(){

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setSigle(String sigle) {
        this.sigle = sigle;
    }

    public void setSalaire(Double salaire) { 
        this.salaire = salaire; 
    }

    /*Getters*/
    public int getId() {
        return id;
    }
    
    public Departement getDepartement() {
        return departement;
    }
    
    public String getLibelle() {
        return libelle;
    }

    public String getSigle() {
        return sigle;
    }
    
    public Double getSalaire() {
        return salaire;
    }
    
}
