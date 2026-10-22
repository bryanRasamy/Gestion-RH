package gestion.entreprise.service;

import gestion.entreprise.dto.EvaluationBudget;
import gestion.entreprise.dto.MontantDemande;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.GrilleBudget;
import gestion.entreprise.modele.HistoriqueBudgetAnnuel;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.GrilleBudgetRepository;
import gestion.entreprise.repository.HistoriqueBudgetAnnuelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** Calcul des montants de demande et cotation par rapport au budget annuel. */
@Service
public class BudgetService {

    private final DetailDemandeRepository detailRepository;
    private final GrilleBudgetRepository grilleRepository;
    private final HistoriqueBudgetAnnuelRepository budgetRepository;

    public BudgetService(DetailDemandeRepository detailRepository,
                         GrilleBudgetRepository grilleRepository,
                         HistoriqueBudgetAnnuelRepository budgetRepository) {
        this.detailRepository = detailRepository;
        this.grilleRepository = grilleRepository;
        this.budgetRepository = budgetRepository;
    }

    /** Montant min et max d'une demande (somme nbr_personnel x fourchette de la grille la plus recente). */
    public MontantDemande montantDemande(int idDemande) {
        double min = 0;
        double max = 0;
        List<DetailDemande> details = detailRepository.findByIdDemande(idDemande);
        LocalDate aujourdHui = LocalDate.now();
        for (DetailDemande detail : details) {
            GrilleBudget grille = grilleRepository
                    .findTopByIdPosteAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(
                            detail.getIdPoste(), aujourdHui);
            if (grille != null) {
                min += grille.getBudgetMin() * detail.getNbrPersonnel();
                max += grille.getBudgetMax() * detail.getNbrPersonnel();
            }
        }
        return new MontantDemande(min, max);
    }

    /** Budget global annuel retenu (ligne la plus recente <= aujourd'hui). */
    public double budgetCourant() {
        HistoriqueBudgetAnnuel budget = budgetRepository
                .findTopByDateEffectiveLessThanEqualOrderByDateEffectiveDesc(LocalDate.now());
        return budget == null ? 0 : budget.getValeur();
    }

    /** Cotation d'une demande : OK, A_REVOIR ou NON. */
    public EvaluationBudget evaluer(int idDemande) {
        MontantDemande montant = montantDemande(idDemande);
        double budget = budgetCourant();
        if (montant.min() > budget) {
            return EvaluationBudget.NON;
        }
        if (montant.max() > budget) {
            return EvaluationBudget.A_REVOIR;
        }
        return EvaluationBudget.OK;
    }
}
