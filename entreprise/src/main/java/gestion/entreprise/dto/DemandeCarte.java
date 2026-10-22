package gestion.entreprise.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Carte de demande affichee dans les listes de validation : informations
 * generales, montant et lignes de detail (pour la demande de revision).
 */
public class DemandeCarte {

    private final int id;
    private final String matricule;
    private final int idEmployer;
    private final String demandeur;
    private final LocalDate dateLimite;
    private final String statut;
    private final LocalDate statutDate;
    private final String statutMotif;
    private final Double montantMin;
    private final Double montantMax;
    private final Double budget;
    private final String evaluation;
    private final List<LigneDemande> lignes;

    public DemandeCarte(int id, String matricule, int idEmployer, String demandeur,
                        LocalDate dateLimite, String statut, LocalDate statutDate, String statutMotif,
                        Double montantMin, Double montantMax, Double budget, String evaluation,
                        List<LigneDemande> lignes) {
        this.id = id;
        this.matricule = matricule;
        this.idEmployer = idEmployer;
        this.demandeur = demandeur;
        this.dateLimite = dateLimite;
        this.statut = statut;
        this.statutDate = statutDate;
        this.statutMotif = statutMotif;
        this.montantMin = montantMin;
        this.montantMax = montantMax;
        this.budget = budget;
        this.evaluation = evaluation;
        this.lignes = lignes;
    }

    public int getId() { return id; }
    public String getMatricule() { return matricule; }
    public int getIdEmployer() { return idEmployer; }
    public String getDemandeur() { return demandeur; }
    public LocalDate getDateLimite() { return dateLimite; }
    public String getStatut() { return statut; }
    public LocalDate getStatutDate() { return statutDate; }
    public String getStatutMotif() { return statutMotif; }
    public Double getMontantMin() { return montantMin; }
    public Double getMontantMax() { return montantMax; }
    public Double getBudget() { return budget; }
    public String getEvaluation() { return evaluation; }
    public List<LigneDemande> getLignes() { return lignes; }

    /** Statut normalise pour les classes CSS (ex : {@code a-revoir}). */
    public String getStatutSlug() {
        return statut == null ? "" : statut.toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    /** Classe CSS du badge correspondant au statut courant. */
    public String getStatutClasse() {
        return switch (getStatutSlug()) {
            case "cree", "valider-rh", "valider-finance", "valider-dg" -> "badge--pending";
            case "a-revoir" -> "badge--review";
            case "refuser", "abandone", "expire" -> "badge--refused";
            default -> "badge--accepted";
        };
    }

    /** Classe CSS de la cotation budgetaire. */
    public String getEvaluationClasse() {
        if (evaluation == null) {
            return "";
        }
        return switch (evaluation) {
            case "Validable" -> "budget__flag--ok";
            case "A revoir" -> "budget__flag--a-revoir";
            default -> "budget__flag--non";
        };
    }

    /** Vrai si la demande ne peut pas etre validee (budget depasse). */
    public boolean estBloqueeParBudget() {
        return "Non validable".equals(evaluation);
    }
}
