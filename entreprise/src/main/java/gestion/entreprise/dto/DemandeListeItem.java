package gestion.entreprise.dto;

import java.time.LocalDate;

/** Ligne d'une demande exposee dans les listes de validation. */
public class DemandeListeItem {

    private final int id;
    private final String matricule;
    private final int idEmployer;
    private final LocalDate dateLimite;
    private final String statut;
    private final LocalDate statutDate;
    private final String statutMotif;
    private final Double montantMin;
    private final Double montantMax;
    private final Double budget;
    private final String evaluation;
    private final int nbDetails;

    public DemandeListeItem(int id, String matricule, int idEmployer, LocalDate dateLimite,
                            String statut, LocalDate statutDate, String statutMotif,
                            Double montantMin, Double montantMax, Double budget,
                            String evaluation, int nbDetails) {
        this.id = id;
        this.matricule = matricule;
        this.idEmployer = idEmployer;
        this.dateLimite = dateLimite;
        this.statut = statut;
        this.statutDate = statutDate;
        this.statutMotif = statutMotif;
        this.montantMin = montantMin;
        this.montantMax = montantMax;
        this.budget = budget;
        this.evaluation = evaluation;
        this.nbDetails = nbDetails;
    }

    public int getId() { return id; }
    public String getMatricule() { return matricule; }
    public int getIdEmployer() { return idEmployer; }
    public LocalDate getDateLimite() { return dateLimite; }
    public String getStatut() { return statut; }
    public LocalDate getStatutDate() { return statutDate; }
    public String getStatutMotif() { return statutMotif; }
    public Double getMontantMin() { return montantMin; }
    public Double getMontantMax() { return montantMax; }
    public Double getBudget() { return budget; }
    public String getEvaluation() { return evaluation; }
    public int getNbDetails() { return nbDetails; }
}
