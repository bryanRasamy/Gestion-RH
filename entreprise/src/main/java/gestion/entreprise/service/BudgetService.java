package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    @Autowired
    private DetailDemandeRepository detailRepository;

    @Autowired
    private GrilleBudgetRepository grilleRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    public double[] montantDemande(int idDemande) {
        List<DetailDemande> details = detailRepository.findAllByDemande_Id(idDemande);
        double minimum = 0;
        double maximum = 0;
        for (DetailDemande detail : details) {
            GrilleBudget grille = grilleRepository.findFirstByPoste_IdAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(
                detail.getPoste().getId(), LocalDate.now());
            if (grille == null) {
                throw new RuntimeException("Aucune grille budget n'est définie pour le poste " + detail.getPoste().getLibelle());
            }
            minimum += detail.getNbrPersonnel() * grille.getBudgetMin();
            maximum += detail.getNbrPersonnel() * grille.getBudgetMax();
        }
        return new double[] {minimum, maximum};
    }

    public double budgetDisponible() {
        Parametre parametre = parametreRepository.findFirstByCodeAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(
            "BUDGET", LocalDate.now());
        if (parametre == null) {
            throw new RuntimeException("Le budget global n'est pas configuré");
        }
        return parametre.getValeur();
    }

    public String evaluer(int idDemande) {
        double[] montant = montantDemande(idDemande);
        double budget = budgetDisponible();
        if (montant[1] <= budget) {
            return "OK";
        }
        if (montant[0] <= budget) {
            return "A_REVOIR";
        }
        return "NON";
    }
}
