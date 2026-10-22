package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "test_technique_niveau")
public class TestTechniqueNiveau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "niveau", nullable = false, unique = true)
    private int niveau;

    @Column(name = "libelle", nullable = false, length = 50)
    private String libelle;

    public TestTechniqueNiveau() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNiveau() { return niveau; }
    public void setNiveau(int niveau) { this.niveau = niveau; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}