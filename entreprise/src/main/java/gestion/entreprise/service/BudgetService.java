package gestion.entreprise.service;

import gestion.entreprise.modele.GrilleBudget;
import gestion.entreprise.modele.HistoriqueBudgetAnnuel;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.GrilleBudgetRepository;
import gestion.entreprise.repository.HistoriqueBudgetAnnuelRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class BudgetService {
	private final DetailDemandeRepository detailRepository;
	private final GrilleBudgetRepository grilleRepository;
	private final HistoriqueBudgetAnnuelRepository budgetRepository;

	public BudgetService(DetailDemandeRepository detailRepository, GrilleBudgetRepository grilleRepository,
						 HistoriqueBudgetAnnuelRepository budgetRepository) {
		this.detailRepository = detailRepository;
		this.grilleRepository = grilleRepository;
		this.budgetRepository = budgetRepository;
	}

	public double montantDemande(int idDemande) {
		double montant = 0;
		List<DetailDemande> details = detailRepository.findByIdDemande(idDemande);
		for (DetailDemande detail : details) {
			GrilleBudget grille = grilleRepository
					.findTopByIdPosteAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(
							detail.getIdPoste(), LocalDate.now());
			if (grille != null) {
				montant += grille.getBudgetMax() * detail.getNbrPersonnel();
			}
		}
		return montant;
	}

	public double budgetCourant() {
		HistoriqueBudgetAnnuel budget = budgetRepository
				.findTopByDateEffectiveLessThanEqualOrderByDateEffectiveDesc(LocalDate.now());
		return budget == null ? 0 : budget.getValeur();
	}

	public boolean estDansBudget(int idDemande) {
		return montantDemande(idDemande) <= budgetCourant();
	}
}
