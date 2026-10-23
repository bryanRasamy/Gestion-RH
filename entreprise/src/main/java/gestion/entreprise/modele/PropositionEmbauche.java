package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "proposition_embauche")
public class PropositionEmbauche {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;

    @ManyToOne @JoinColumn(name = "id_candidat", nullable = false)
    private Candidat candidat;

    @ManyToOne @JoinColumn(name = "id_contrat_type", nullable = false)
    private ContratType contratType;

    @Column(name = "salaire", nullable = false)
    private BigDecimal salaire;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "lieu_travail", nullable = false)
    private String lieuTravail;

    @Column(name = "horaires", nullable = false)
    private String horaires;

    @Column(name = "duree_essai_mois", nullable = false)
    private int dureeEssaiMois;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @ManyToOne @JoinColumn(name = "id_proposition_embauche_statut", nullable = false)
    private PropositionEmbaucheStatut statut;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "proposition", cascade = CascadeType.ALL)
    private List<PropositionEmbaucheQuestion> questions;

    @OneToMany(mappedBy = "proposition", cascade = CascadeType.ALL)
    private List<PropositionEmbaucheNegociation> negociations;

    public PropositionEmbauche() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }
    public ContratType getContratType() { return contratType; }
    public void setContratType(ContratType contratType) { this.contratType = contratType; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }
    public String getLieuTravail() { return lieuTravail; }
    public void setLieuTravail(String lieuTravail) { this.lieuTravail = lieuTravail; }
    public String getHoraires() { return horaires; }
    public void setHoraires(String horaires) { this.horaires = horaires; }
    public int getDureeEssaiMois() { return dureeEssaiMois; }
    public void setDureeEssaiMois(int dureeEssaiMois) { this.dureeEssaiMois = dureeEssaiMois; }
    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }
    public PropositionEmbaucheStatut getStatut() { return statut; }
    public void setStatut(PropositionEmbaucheStatut statut) { this.statut = statut; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<PropositionEmbaucheQuestion> getQuestions() { return questions; }
    public void setQuestions(List<PropositionEmbaucheQuestion> questions) { this.questions = questions; }
    public List<PropositionEmbaucheNegociation> getNegociations() { return negociations; }
    public void setNegociations(List<PropositionEmbaucheNegociation> negociations) { this.negociations = negociations; }
}