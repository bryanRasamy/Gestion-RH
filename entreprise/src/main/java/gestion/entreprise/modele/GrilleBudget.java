package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "grille_budget")
public class GrilleBudget {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "id_poste", nullable = false) private int idPoste;
    @Column(name = "budget_min", nullable = false) private Double budgetMin;
    @Column(name = "budget_max", nullable = false) private Double budgetMax;
    @Column(name = "date_effective", nullable = false) private LocalDate dateEffective;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdPoste() { return idPoste; }
    public void setIdPoste(int idPoste) { this.idPoste = idPoste; }
    public Double getBudgetMin() { return budgetMin; }
    public void setBudgetMin(Double budgetMin) { this.budgetMin = budgetMin; }
    public Double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(Double budgetMax) { this.budgetMax = budgetMax; }
    public LocalDate getDateEffective() { return dateEffective; }
    public void setDateEffective(LocalDate dateEffective) { this.dateEffective = dateEffective; }
}