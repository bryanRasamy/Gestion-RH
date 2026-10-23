package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "reponse_type")
public class ReponseType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(name = "libelle", nullable = false, unique = true) private String libelle;
    public ReponseType() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}