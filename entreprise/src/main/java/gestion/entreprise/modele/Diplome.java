package gestion.entreprise.modele;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "diplome")
public class Diplome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "annee_etude", nullable = false)
    private int anneeEtude;

    @Column(name = "nom_diplome", nullable = false, length = 100)
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
}
