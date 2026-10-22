package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "historique_budget_annuel")
public class HistoriqueBudgetAnnuel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(nullable = false, columnDefinition = "numeric(10,2)") private Double valeur;
    @Column(name = "date_effective", nullable = false) private LocalDate dateEffective;
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Double getValeur() { return valeur; }
    public void setValeur(Double valeur) { this.valeur = valeur; }
    public LocalDate getDateEffective() { return dateEffective; }
    public void setDateEffective(LocalDate dateEffective) { this.dateEffective = dateEffective; }
}