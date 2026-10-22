package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "sigle", nullable = false, unique = true, length = 20)
    private String sigle;

    /*Constructeur*/
    public Role() {

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
