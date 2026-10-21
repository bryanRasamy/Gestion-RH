package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "libelle", nullable = false, length = 100)
    private String libelle;

    @Column(name = "sigle", nullable = false, length = 100)
    private String sigle;

    @OneToMany(mappedBy = "status")
    private List<HistoriqueDemandeStatus> historiques;

    /*Constructeur*/
    public Status() {
 
    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setSigle(String sigle) {
        this.sigle = sigle;
    }

    public void setHistoriques(List<HistoriqueDemandeStatus> historiques) {
        this.historiques = historiques;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getSigle() {
        return sigle;
    }

    public List<HistoriqueDemandeStatus> getHistoriques() {
        return historiques;
    }
}
    
