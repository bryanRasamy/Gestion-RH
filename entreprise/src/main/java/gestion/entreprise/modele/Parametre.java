package gestion.entreprise.modele;

import jakarta.persistence.*;

@Entity
@Table(name = "parametre")
public class Parametre {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @Column(name = "code", nullable = false, unique = true) private String code;
    @Column(name = "valeur", nullable = false, columnDefinition = "TEXT") private String valeur;
    public Parametre() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getValeur() { return valeur; }
    public void setValeur(String valeur) { this.valeur = valeur; }
}