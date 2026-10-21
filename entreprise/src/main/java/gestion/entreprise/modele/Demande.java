package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 50, unique = true)
    private String matricule;

    @Column(name = "id_employer", nullable = false)
    private int idEmployer;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "nbr_personnel_propose")
    private Integer nbrPersonnelPropose;

    @Column(nullable = false)
    private boolean traite;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public int getIdEmployer() { return idEmployer; }
    public void setIdEmployer(int idEmployer) { this.idEmployer = idEmployer; }

    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Integer getNbrPersonnelPropose() { return nbrPersonnelPropose; }
    public void setNbrPersonnelPropose(Integer nbrPersonnelPropose) { this.nbrPersonnelPropose = nbrPersonnelPropose; }

    public boolean isTraite() { return traite; }
    public void setTraite(boolean traite) { this.traite = traite; }
}