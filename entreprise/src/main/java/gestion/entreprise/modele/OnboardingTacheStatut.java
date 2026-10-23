package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_tache_statut")
public class OnboardingTacheStatut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;

    public OnboardingTacheStatut() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
