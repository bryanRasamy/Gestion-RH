package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "employer")
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "matricule", nullable = false, unique = true)
    private String matricule;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "compte")
    private String compte;

    @ManyToOne
    @JoinColumn(name = "id_poste")
    private Poste poste;

    @Column(name = "salaire")
    private Double salaire;

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
    
}
