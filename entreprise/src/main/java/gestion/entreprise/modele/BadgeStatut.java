package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "badge_statut")
public class BadgeStatut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    public BadgeStatut() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
