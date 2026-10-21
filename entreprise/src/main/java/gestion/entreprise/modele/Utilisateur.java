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

    @OneToOne
    @JoinColumn(name = "id_employer")
    private Employer employer;

    @Column(name = "id_role")
    private int idRole;

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

    public void setIdRole(int idRole) { this.idRole = idRole; }

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

    public int getIdRole() { return idRole; }
    
}
