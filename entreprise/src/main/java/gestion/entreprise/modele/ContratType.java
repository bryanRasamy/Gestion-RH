package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "contrat_type")
public class ContratType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(name = "libelle", nullable = false, unique = true) private String libelle;
    @Column(name = "duree_max_mois") private Integer dureeMaxMois;
    @Column(name = "nb_renouvellement_max") private Integer nbRenouvellementMax = 0;

    public ContratType() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Integer getDureeMaxMois() { return dureeMaxMois; }
    public void setDureeMaxMois(Integer dureeMaxMois) { this.dureeMaxMois = dureeMaxMois; }
    public Integer getNbRenouvellementMax() { return nbRenouvellementMax; }
    public void setNbRenouvellementMax(Integer nbRenouvellementMax) { this.nbRenouvellementMax = nbRenouvellementMax; }
}