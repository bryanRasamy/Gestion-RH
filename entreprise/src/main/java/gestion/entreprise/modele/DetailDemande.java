package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "detail_demande")
public class DetailDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "id_demande", nullable = false)
    private int idDemande;

    @Column(name = "id_poste", nullable = false)
    private int idPoste;

    @Column(name = "id_departement", nullable = false)
    private int idDepartement;

    @Column(name = "nbr_personnel", nullable = false)
    private int nbrPersonnel;

    @Column(columnDefinition = "TEXT")
    private String description;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public int getIdPoste() { return idPoste; }
    public void setIdPoste(int idPoste) { this.idPoste = idPoste; }

    public int getIdDepartement() { return idDepartement; }
    public void setIdDepartement(int idDepartement) { this.idDepartement = idDepartement; }

    public int getNbrPersonnel() { return nbrPersonnel; }
    public void setNbrPersonnel(int nbrPersonnel) { this.nbrPersonnel = nbrPersonnel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMotif() { return description; }
    public void setMotif(String motif) { this.description = motif; }
}