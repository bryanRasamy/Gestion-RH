package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "detail_demande")
public class DetailDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;

    @ManyToOne
    @JoinColumn(name = "id_departement", nullable = false)
    private Departement departement;

    @Column(name = "nbr_personnel", nullable = false)
    private int nbrPersonnel;

    @Column(name = "motif", columnDefinition = "TEXT")
    private String motif;

    @ManyToOne
    @JoinColumn(name = "id_diplome")
    private Diplome diplome;

    @Column(name = "annee_experience", nullable = false)
    private int anneeExperience;

    // Frontoffice : candidatures liées à ce détail de demande
    @OneToMany(mappedBy = "detailDemande")
    private List<Candidat> candidats;

    /*Constructeur*/
    public DetailDemande() {
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

    public void setCandidats(List<Candidat> candidats) {
        this.candidats = candidats;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public Demande getDemande() {
        return demande;
    }

    public Poste getPoste() {
        return poste;
    }

    public Departement getDepartement() {
        return departement;
    }

    public int getNbrPersonnel() {
        return nbrPersonnel;
    }

    public String getMotif() {
        return motif;
    }

    public Diplome getDiplome() {
        return diplome;
    }

    public int getAnneeExperience() {
        return anneeExperience;
    }

    public List<Candidat> getCandidats() {
        return candidats;
    }

    /**
     * Frontoffice : nombre de postes pourvus pour ce détail.
     * Un candidat compte comme pourvu s'il n'est ni refusé, ni annulé, ni à revoir.
     */
    public int getNombrePourvus() {
        int total = 0;
        if (candidats != null) {
            for (Candidat c : candidats) {
                if (c != null && c.getStatus() != null
                        && !"Refuser".equalsIgnoreCase(c.getStatus())
                        && !"Annuler".equalsIgnoreCase(c.getStatus())
                        && !"A Revoir".equalsIgnoreCase(c.getStatus())) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Frontoffice : indique si le poste est entièrement pourvu.
     */
    public boolean isCloturee() {
        return getNombrePourvus() >= nbrPersonnel;
    }
}