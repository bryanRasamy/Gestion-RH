package gestion.entreprise.vue;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "v_historique_demande_status")
public class HistoriqueDemandeStatusView {

    @Id
    @Column(name = "id")
    private int id;

    @Column(name = "id_demande")
    private int idDemande;

    @Column(name = "id_status")
    private int idStatus;

    @Column(name = "id_employer_evaluateur")
    private int idEmployerEvaluateur;

    @Column(name = "nom_evaluateur")
    private String nomEvaluateur;

    @Column(name = "prenom_evaluateur")
    private String prenomEvaluateur;

    @Column(name = "matricule")
    private String matricule;

    @Column(name = "id_employer")
    private int idEmployer;

    @Column(name = "libelle")
    private String libelle;

    @Column(name = "date")
    private LocalDate date;
    
    /*Constructeur*/
    public HistoriqueDemandeStatusView() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    public void setIdEmployerEvaluateur(int idEmployerEvaluateur) {
        this.idEmployerEvaluateur = idEmployerEvaluateur;
    }

    public void setNomEvaluateur(String nomEvaluateur) {
        this.nomEvaluateur = nomEvaluateur;
    }

    public void setPrenomEvaluateur(String prenomEvaluateur) {
        this.prenomEvaluateur = prenomEvaluateur;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public void setIdEmployer(int idEmployer) {
        this.idEmployer = idEmployer;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public int getIdEmployerEvaluateur() {
        return this.idEmployerEvaluateur;
    }

    public String getNomEvaluateur() {
        return this.nomEvaluateur;
    }

    public String getPrenomEvaluateur() {
        return this.prenomEvaluateur;
    }

    public String getMatricule() {
        return matricule;
    }

    public int getIdEmployer() {
        return idEmployer;
    }

    public String getLibelle() {
        return libelle;
    }

    public LocalDate getDate() {
        return date;
    }
}
