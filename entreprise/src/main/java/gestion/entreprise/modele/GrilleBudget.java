package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "grille_budget")
public class GrilleBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "id_poste", nullable = false)
    private Poste poste;

    @Column(name = "budget_min", nullable = false)
    private double budgetMin;

    @Column(name = "budget_max", nullable = false)
    private double budgetMax;

    @Column(name = "date_effective", nullable = false)
    private LocalDate dateEffective;

    /*Constructeur*/
    public GrilleBudget() {

    }

    /*Setters*/
    public void setId(int id) {
        this.id = id;
    }

    public void setPoste(Poste poste) {
        this.poste = poste;
    }

    public void setBudgetMin(double budgetMin) {
        this.budgetMin = budgetMin;
    }

    public void setBudgetMax(double budgetMax) {
        this.budgetMax = budgetMax;
    }

    public void setDateEffective(LocalDate dateEffective) {
        this.dateEffective = dateEffective;
    }

    /*Getters*/
    public int getId() {
        return id;
    }

    public Poste getPoste() {
        return poste;
    }

    public double getBudgetMin() {
        return budgetMin;
    }

    public double getBudgetMax() {
        return budgetMax;
    }

    public LocalDate getDateEffective() {
        return dateEffective;
    }
}
