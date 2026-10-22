package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CandidatureWorkflowService {

    @Autowired private CandidatRepository candidatRepository;
    @Autowired private HistoriqueCandidatStatutRepository historiqueRepository;
    @Autowired private UtilisateurRepository utilisateurRepository;

    /** Change le statut + ajoute une ligne d'historique. */
    @Transactional
    public void changerStatut(Candidat candidat, String nouveauStatut,
                              int idUtilisateur, String motif) throws Exception {
        if (candidat == null) throw new Exception("Candidature invalide.");
        Utilisateur user = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new Exception("Utilisateur introuvable"));

        HistoriqueCandidatStatut hist = new HistoriqueCandidatStatut();
        hist.setCandidat(candidat);
        hist.setAncienStatut(candidat.getStatus());
        hist.setNouveauStatut(nouveauStatut);
        hist.setMotif(motif);
        hist.setUtilisateur(user);
        hist.setDateChangement(LocalDateTime.now());
        historiqueRepository.save(hist);

        candidat.setStatus(nouveauStatut);
        candidatRepository.save(candidat);
    }

    public List<HistoriqueCandidatStatut> getHistorique(int idCandidat) {
        return historiqueRepository.findByCandidatIdOrderByDateChangementAsc(idCandidat);
    }

    /** Label lisible pour l'UI. */
    public static String libelleStatut(String statut) {
        if (statut == null) return "";
        switch (statut) {
            case "EN_ATTENTE":      return "En attente";
            case "CV_VALIDE":       return "CV validé";
            case "EN_TEST":         return "Tests en cours";
            case "TESTS_REUSSIS":   return "Tests réussis";
            case "TEST_ECHOUE":     return "Test échoué";
            case "EN_ENTRETIEN":    return "Entretien";
            case "ACCEPTE":         return "Accepté";
            case "REFUSE":          return "Refusé";
            default:                return statut;
        }
    }
}