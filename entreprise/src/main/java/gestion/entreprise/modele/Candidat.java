package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "candidat")
public class Candidat implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_detail_demande", nullable = false)
    private DetailDemande detailDemande;

    @Column(name = "cv_nom", nullable = false, length = 255)
    private String cvNom;

    @Column(name = "lm_nom", length = 255)
    private String lmNom;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "En attente";

    @Column(name = "date_postulation", nullable = false)
    private LocalDate datePostulation = LocalDate.now();

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "titre", nullable = false, length = 100)
    private String titre;

    @Column(name = "resume", columnDefinition = "TEXT")
    private String resume;

    @Column(name = "dernier_poste", length = 100)
    private String dernierPoste;

    @Column(name = "entreprise", length = 100)
    private String entreprise;

    @Column(name = "id_diplome")
    private Integer idDiplome;

    @Column(name = "annee_experience_prof")
    private Integer anneeExperienceProf;

    @Column(name = "ecole", length = 100)
    private String ecole;

    @Transient
private int prochainNiveau;

    public int getProchainNiveau() {
        return prochainNiveau;
    }

    public void setProchainNiveau(int prochainNiveau) {
        this.prochainNiveau = prochainNiveau;
    }

    public Candidat() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public void setDetailDemande(DetailDemande detailDemande) {
        this.detailDemande = detailDemande;
    }

    public void setCvNom(String cvNom) {
        this.cvNom = cvNom;
    }

    public void setLmNom(String lmNom) {
        this.lmNom = lmNom;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDatePostulation(LocalDate datePostulation) {
        this.datePostulation = datePostulation;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public void setDernierPoste(String dernierPoste) {
        this.dernierPoste = dernierPoste;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public void setIdDiplome(Integer idDiplome) {
        this.idDiplome = idDiplome;
    }

    public void setAnneeExperienceProf(Integer anneeExperienceProf) {
        this.anneeExperienceProf = anneeExperienceProf;
    }

    public void setEcole(String ecole) {
        this.ecole = ecole;
    }

    public int getId() {
        return id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public DetailDemande getDetailDemande() {
        return detailDemande;
    }

    public String getCvNom() {
        return cvNom;
    }

    public String getLmNom() {
        return lmNom;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDatePostulation() {
        return datePostulation;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getTitre() {
        return titre;
    }

    public String getResume() {
        return resume;
    }

    public String getDernierPoste() {
        return dernierPoste;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public Integer getIdDiplome() {
        return idDiplome;
    }

    public Integer getAnneeExperienceProf() {
        return anneeExperienceProf;
    }

    public String getEcole() {
        return ecole;
    }
}
