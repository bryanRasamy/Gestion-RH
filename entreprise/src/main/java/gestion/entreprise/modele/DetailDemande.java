package gestion.entreprise.modele;

import jakarta.persistence.*;

/** Ligne d'une demande : un poste, un nombre de personnes et un motif/description. */
@Entity
@Table(name = "detail_demande")
public class DetailDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_demande", nullable = false)
    private int idDemande;

    @Column(name = "id_poste", nullable = false)
    private int idPoste;

    @Column(name = "id_departement", nullable = false)
    private int idDepartement;

    @Column(name = "nbr_personnel", nullable = false)
    private int nbrPersonnel;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "id_diplome", nullable = false)
    private int idDiplome;

    @Column(name = "annee_experience", nullable = false)
    private int anneeExperience;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public int getIdPoste() {
        return idPoste;
    }

    public void setIdPoste(int idPoste) {
        this.idPoste = idPoste;
    }

    public int getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(int idDepartement) {
        this.idDepartement = idDepartement;
    }

    public int getNbrPersonnel() {
        return nbrPersonnel;
    }

    public void setNbrPersonnel(int nbrPersonnel) {
        this.nbrPersonnel = nbrPersonnel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdDiplome() {
        return idDiplome;
    }

    public void setIdDiplome(int idDiplome) {
        this.idDiplome = idDiplome;
    }

    public int getAnneeExperience() {
        return anneeExperience;
    }

    public void setAnneeExperience(int anneeExperience) {
        this.anneeExperience = anneeExperience;
    }
}
