package gestion.entreprise.modele;

import jakarta.persistence.*;
import gestion.entreprise.modele.TypeRole;

/** Referentiel des roles applicatifs (RH, Manager, Finance, DG). */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "roles", nullable = false, unique = true, length = 30)
    private String libelle;

    public Role() {
    }

    public Role(String libelle) {
        this.libelle = libelle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public TypeRole type() {
        return TypeRole.fromLibelle(libelle);
    }

    @Override
    public String toString() {
        return libelle;
    }
}
