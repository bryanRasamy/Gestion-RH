package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_budget_annuel")
public class HistoriqueBudgetAnnuel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "valeur", nullable = false)
    private double valeur;

    @Column(name = "date_effective", nullable = false)
    private LocalDate dateEffective;

    /*Constructeur*/
    public HistoriqueBudgetAnnuel() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setValeur(double valeur) {
        this.valeur = valeur;
    }

    public void setDateEffective(LocalDate dateEffective) {
        this.dateEffective = dateEffective;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public double getValeur() {
        return valeur;
    }

    public LocalDate getDateEffective() {
        return dateEffective;
    }
}
