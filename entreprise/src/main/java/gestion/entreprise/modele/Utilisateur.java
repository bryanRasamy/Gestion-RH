package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateur")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mdp", nullable = false)
    private String mdp;

    @ManyToOne
    @JoinColumn(name = "id_employer")
    private Employer employer;

    /*Constructeur*/
    public Utilisateur(){

    }

    /*Setters*/
    public void setId(int id) { 
        this.id = id; 
    }

    public void setEmail(String email) {
        this.email = email; 
    }

    public void setMdp(String mdp) { 
        this.mdp = mdp; 
    }

    public void setEmployer(Employer employer) { 
        this.employer = employer; 
    }

    /*Getters*/
    public int getId() { 
        return id; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public String getMdp() { 
        return mdp; 
    }
    
    public Employer getEmployer() { 
        return employer;
    }
    
}
