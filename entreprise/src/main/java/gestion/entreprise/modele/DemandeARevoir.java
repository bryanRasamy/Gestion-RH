package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande_a_revoir")
public class DemandeARevoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_mpangataka", nullable = false)
    private Employer mpangataka;

    @ManyToOne
    @JoinColumn(name = "id_detail_demande", nullable = false)
    private DetailDemande detailDemande;

    @Column(name = "etape_demandeuse", nullable = false)
    private String etapeDemandeuse;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "commentaire", nullable = false)
    private String commentaire;

    @Column(name = "nbr_personnel_propose", nullable = false)
    private int nbrPersonnelPropose;

    @Column(name = "annuler_poste", nullable = false)
    private boolean annulerPoste;

    @Column(name = "traite", nullable = false)
    private boolean traite;

    public DemandeARevoir() {
    }

    public int getId() {
        return id;
    }

    public Employer getMpangataka() {
        return mpangataka;
    }

    public void setMpangataka(Employer mpangataka) {
        this.mpangataka = mpangataka;
    }

    public DetailDemande getDetailDemande() {
        return detailDemande;
    }

    public void setDetailDemande(DetailDemande detailDemande) {
        this.detailDemande = detailDemande;
    }

    public String getEtapeDemandeuse() {
        return etapeDemandeuse;
    }

    public void setEtapeDemandeuse(String etapeDemandeuse) {
        this.etapeDemandeuse = etapeDemandeuse;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public int getNbrPersonnelPropose() {
        return nbrPersonnelPropose;
    }

    public void setNbrPersonnelPropose(int nbrPersonnelPropose) {
        this.nbrPersonnelPropose = nbrPersonnelPropose;
    }

    public boolean isAnnulerPoste() {
        return annulerPoste;
    }

    public void setAnnulerPoste(boolean annulerPoste) {
        this.annulerPoste = annulerPoste;
    }

    public boolean isTraite() {
        return traite;
    }

    public void setTraite(boolean traite) {
        this.traite = traite;
    }
}
