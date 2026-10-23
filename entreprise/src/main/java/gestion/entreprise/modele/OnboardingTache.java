package gestion.entreprise.modele;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboarding_tache")
public class OnboardingTache {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private int id;
    @ManyToOne @JoinColumn(name = "id_periode_emploi", nullable = false) private PeriodeEmploi periodeEmploi;
    @ManyToOne @JoinColumn(name = "id_onboarding_tache_type", nullable = false) private OnboardingTacheType type;
    @ManyToOne @JoinColumn(name = "id_onboarding_tache_statut", nullable = false) private OnboardingTacheStatut statut;
    @Column(name = "date_realisation") private LocalDateTime dateRealisation;
    @Column(name = "commentaire", columnDefinition = "TEXT") private String commentaire;
    public OnboardingTache() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public PeriodeEmploi getPeriodeEmploi() { return periodeEmploi; }
    public void setPeriodeEmploi(PeriodeEmploi periodeEmploi) { this.periodeEmploi = periodeEmploi; }
    public OnboardingTacheType getType() { return type; }
    public void setType(OnboardingTacheType type) { this.type = type; }
    public OnboardingTacheStatut getStatut() { return statut; }
    public void setStatut(OnboardingTacheStatut statut) { this.statut = statut; }
    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}