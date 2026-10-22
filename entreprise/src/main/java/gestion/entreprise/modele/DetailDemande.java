package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "detail_demande")
public class DetailDemande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_demande")
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_poste")
    private Poste poste;

    @ManyToOne
    @JoinColumn(name = "id_departement")
    private Departement departement;

    @Column(name = "nbr_personnel", nullable = false)
    private int nbrPersonnel;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @ManyToOne
    @JoinColumn(name = "id_diplome")
    private Diplome diplome;

    @Column(name = "annee_experienxe", nullable = false)
    private int anneeExperience;

    /*Constructeur*/
    public DetailDemande(){

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setDemande(Demande demande) {
        this.demande = demande;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public void setDepartement(Departement departement) {
        this.departement = departement;
    }

    public void setNbrPersonnel(int nbrPersonnel) {
        this.nbrPersonnel = nbrPersonnel;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public void setDiplome(Diplome diplome) {
        this.diplome = diplome;
    }

    public void setAnneeExperience(int anneeExperience) {
        this.anneeExperience = anneeExperience;
    }


    /*Getters*/
    public int getId() {
        return id;
    }
    
    public Demande getDemande() {
        return this.demande;
    }

    public Poste getPoste() {
        return this.poste;
    }

    public Departement getDepartement() {
        return this.departement;
    }

    public int getNbrPersonnel() {
        return nbrPersonnel;
    }
    
    public String getMotif() {
        return motif;
    }

    public Diplome getDiplome() {
        return this.diplome;
    }

    public int getAnneeExperience() {
        return this.anneeExperience;
    }
    
}