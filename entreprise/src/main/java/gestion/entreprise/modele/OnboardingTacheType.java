package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "onboarding_tache_type")
public class OnboardingTacheType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(name = "libelle", nullable = false, unique = true, length = 100) private String libelle;
    @Column(name = "ordre", nullable = false) private int ordre = 0;
    @Column(name = "obligatoire", nullable = false) private boolean obligatoire = true;
    @Column(name = "actif", nullable = false) private boolean actif = true;
    public OnboardingTacheType() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }
    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}