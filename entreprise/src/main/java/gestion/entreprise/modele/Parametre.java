package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "parametre")
public class Parametre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false)
    private Double valeur;
    @Column(name = "date_effective", nullable = false)
    private LocalDate dateEffective;

    public int getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
    public Double getValeur() { return valeur; }
    public void setValeur(Double value) { valeur = value; }
    public LocalDate getDateEffective() { return dateEffective; }
    public void setDateEffective(LocalDate value) { dateEffective = value; }
}
