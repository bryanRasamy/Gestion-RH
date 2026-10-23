package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrat")
public class Contrat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @OneToOne
    @JoinColumn(name = "id_proposition_embauche", nullable = false, unique = true)
    private PropositionEmbauche proposition;

    @ManyToOne
    @JoinColumn(name = "id_contrat_type", nullable = false)
    private ContratType contratType;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "salaire", nullable = false)
    private BigDecimal salaire;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Contrat() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PropositionEmbauche getProposition() { return proposition; }
    public void setProposition(PropositionEmbauche proposition) { this.proposition = proposition; }
    public ContratType getContratType() { return contratType; }
    public void setContratType(ContratType contratType) { this.contratType = contratType; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}