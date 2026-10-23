package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeService {

    @Autowired private PeriodeEmploiRepository periodeEmploiRepo;
    @Autowired private AffectationRepository affectationRepo;
    @Autowired private ContratRepository contratRepo;

    /** Un utilisateur est employé s'il a une période d'emploi ACTIVE. */
    public boolean estEmploye(Utilisateur u) {
        if (u == null || u.getEmployer() == null) return false;
        return !periodeEmploiRepo
                .findAllByEmployerIdAndStatutLibelle(u.getEmployer().getId(), "ACTIVE")
                .isEmpty();
    }

    /** Période d'emploi active. */
    public PeriodeEmploi getPeriodeActive(Utilisateur u) {
        if (u == null || u.getEmployer() == null) return null;
        List<PeriodeEmploi> list = periodeEmploiRepo
                .findAllByEmployerIdAndStatutLibelle(u.getEmployer().getId(), "ACTIVE");
        return list.isEmpty() ? null : list.get(0);
    }

    /** Affectation courante (la plus récente de la période active). */
    public Affectation getAffectationCourante(PeriodeEmploi periode) {
        if (periode == null) return null;
        List<Affectation> list = affectationRepo.findByPeriodeEmploiId(periode.getId());
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /** Contrat courant = celui pointé par periode.contratActuel. */
    public Contrat getContratCourant(PeriodeEmploi periode) {
        if (periode == null) return null;
        return getContratActuel(periode);
    }

    /** Historique des contrats pour une période active. */
    public List<Contrat> getContrats(PeriodeEmploi periode) {
        if (periode == null) return List.of();
        return getHistoriqueContrats(periode);
    }

    /** Contrat actuel = celui pointé par periode.contratActuel. Fallback sur l'historique. */
    public Contrat getContratActuel(PeriodeEmploi periode) {
        if (periode == null) return null;
        if (periode.getContratActuel() != null) {
            return periode.getContratActuel();
        }
        // Fallback (anciennes données sans contratActuel)
        List<Contrat> hist = getHistoriqueContrats(periode);
        return hist.isEmpty() ? null : hist.get(hist.size() - 1);
    }

    /** Historique complet des contrats de la période, du plus ancien au plus récent. */
    public List<Contrat> getHistoriqueContrats(PeriodeEmploi periode) {
        if (periode == null) return List.of();
        return contratRepo.findHistoriqueByPeriodeEmploi(periode.getId());
    }
}