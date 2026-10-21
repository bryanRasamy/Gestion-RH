package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(nullable = false, unique = true, length = 30)
    private String sigle;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getSigle() { return sigle; }
    public void setSigle(String sigle) { this.sigle = sigle; }
}
