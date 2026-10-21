package gestion.entreprise.modele;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "employer")
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "matricule", nullable = false, unique = true, length = 100)
    private String matricule;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "compte", length = 100)
    private String compte;

    @ManyToOne
    @JoinColumn(name = "id_poste")
    private Poste poste;

    @Column(name = "salaire")
    private Double salaire;

    @OneToMany(mappedBy = "employer")
    private List<Demande> demandes;

    @OneToMany(mappedBy = "employer")
    private List<HistoriqueDemandeStatus> historiqueDemandeStatus;

    /*Constructeur*/
    public Employer(){

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setCompte(String compte) {
        this.compte = compte;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public void setDemandes(List<Demande> demandes) {
        this.demandes = demandes;
    }

    public void setHistoriqueDemandeStatus(List<HistoriqueDemandeStatus> historiqueDemandeStatus) {
        this.historiqueDemandeStatus = historiqueDemandeStatus;
    }

    /*Getters*/
    public int getId() {
        return id;
    }
    
    public String getMatricule() {
        return matricule;
    }
    
    public String getNom() {
        return nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public String getCompte() {
        return compte;
    }
    
    public Poste getPoste() {
        return poste;
    }
    
    public Double getSalaire() {
        return salaire;
    }

    public List<Demande> setDemandes() {
        return this.demandes;
    }

    public List<HistoriqueDemandeStatus> setHistoriqueDemandeStatus() {
        return this.historiqueDemandeStatus;
    }
    
}
