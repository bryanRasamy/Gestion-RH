package gestion.entreprise.modele;

import jakarta.persistence.*;

/** Diplome requis pour un poste demande. */
@Entity
@Table(name = "diplome")
public class Diplome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "annee_etude", nullable = false)
    private int anneeEtude;

    @Column(name = "nom_diplome", nullable = false)
    private String nomDiplome;

    public Diplome() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnneeEtude() {
        return anneeEtude;
    }

    public void setAnneeEtude(int anneeEtude) {
        this.anneeEtude = anneeEtude;
    }

    public String getNomDiplome() {
        return nomDiplome;
    }

    public void setNomDiplome(String nomDiplome) {
        this.nomDiplome = nomDiplome;
    }

    @Override
    public String toString() {
        return nomDiplome + " (" + anneeEtude + " ans d'etude)";
    }
}
