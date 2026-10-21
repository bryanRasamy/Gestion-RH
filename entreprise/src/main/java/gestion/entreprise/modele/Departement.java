package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "departement")
public class Departement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    @Column(name = "sigle", nullable = false, unique = true)
    private String sigle;

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

}
