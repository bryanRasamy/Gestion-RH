package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Ligne de detail d'une demande remise a revoir.
 *
 * <p>{@code idDemandeur} identifie l'employe (RH, Finance ou DG) qui demande
 * la revision. Une revision en attente se deduit du statut courant de la
 * demande : il n'existe pas de colonne {@code traite}.</p>
 */
@Entity
@Table(name = "demande_a_revoir")
public class DemandeARevoir {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_demandeur", nullable = false)
    private int idDemandeur;

    @Column(name = "id_demande_details", nullable = false)
    private int idDemandeDetails;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "nbr_personnel_propose")
    private Integer nbrPersonnelPropose;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdDemandeur() { return idDemandeur; }
    public void setIdDemandeur(int idDemandeur) { this.idDemandeur = idDemandeur; }
    public int getIdDemandeDetails() { return idDemandeDetails; }
    public void setIdDemandeDetails(int idDemandeDetails) { this.idDemandeDetails = idDemandeDetails; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Integer getNbrPersonnelPropose() { return nbrPersonnelPropose; }
    public void setNbrPersonnelPropose(Integer nbrPersonnelPropose) { this.nbrPersonnelPropose = nbrPersonnelPropose; }
}
