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

    @Column(name = "annee_experience", nullable = false)
    private int anneeExperience;

    @OneToMany(mappedBy = "detailDemande")
    private List<Candidat> candidats;

    public DetailDemande() {
    }

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

    public void setAnneeExperience(int anneeExperience) {
        this.anneeExperience = anneeExperience;
    }

    public void setCandidats(List<Candidat> candidats) {
        this.candidats = candidats;
    }

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

    public int getAnneeExperience() {
        return anneeExperience;
    }

    public List<Candidat> getCandidats() {
        return candidats;
    }

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

    public boolean isCloturee() {
        return getNombrePourvus() >= nbrPersonnel;
    }
}
