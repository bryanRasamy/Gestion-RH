package gestion.entreprise.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.vue.*;

import java.time.LocalDate;

@Service
public class HistoriqueDemandeStatusService {

    @Autowired 
    private HistoriqueDemandeStatusViewRepository historiqueDemandeStatusViewRepository;

    @Autowired
    private HistoriqueDemandeStatusRepository historiqueDemandeStatusRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<HistoriqueDemandeStatusView> getDemandesRecent() {
        return historiqueDemandeStatusViewRepository.findLatestStatusPerDemand();
    }

    public List<HistoriqueDemandeStatusView> getDemandesByEmployer(int idEmployer) {
        return historiqueDemandeStatusViewRepository.findLatestStatusPerDemandByEmployer(idEmployer);
    }

    /**
     * Workflow: demandes dont le DERNIER statut = sigleStatus
     * ET dont l'évaluateur (id_employer_evaluateur) est un utilisateur ayant le sigleRole.
     *
     * On résout les employer ids du rôle en Java, puis on passe au repository.
     */
    public List<HistoriqueDemandeStatusView> getDemandesByStatusAndRole(String sigleStatus, String sigleRole) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findByRoleSigle(sigleRole);

        if (utilisateurs == null || utilisateurs.isEmpty()) {
            return new ArrayList<>();
        }

        int[] evaluateurIds = new int[utilisateurs.size()];
        for (int i = 0; i < utilisateurs.size(); i++) {
            evaluateurIds[i] = utilisateurs.get(i).getEmployer().getId();
        }

        return historiqueDemandeStatusViewRepository.findLatestByStatusAndEvaluateurs(sigleStatus, evaluateurIds);
    }

    public List<HistoriqueDemandeStatusView> getByDemande(int idDemande) {
        return historiqueDemandeStatusViewRepository.findAllByDemande(idDemande);
    }

    /**
     * Filtre serveur-side par statut et dates.
     */
    public List<HistoriqueDemandeStatusView> filtrer(List<HistoriqueDemandeStatusView> demandes,
            String statusSigle, LocalDate dateMin, LocalDate dateMax) {

        List<HistoriqueDemandeStatusView> resultat = new ArrayList<>();

        for (HistoriqueDemandeStatusView d : demandes) {
            boolean garder = true;

            if (statusSigle != null && !statusSigle.isEmpty()) {
                if (!d.getSigle().equalsIgnoreCase(statusSigle)) {
                    garder = false;
                }
            }

            if (dateMin != null) {
                if (d.getDate().isBefore(dateMin)) {
                    garder = false;
                }
            }

            if (dateMax != null) {
                if (d.getDate().isAfter(dateMax)) {
                    garder = false;
                }
            }

            if (garder) {
                resultat.add(d);
            }
        }

        return resultat;
    }

    public void changerStatut(Demande demande, Status status, Employer employer, String motif) throws Exception {
        if (demande == null) {
            throw new Exception("Demande invalide.");
        }
        if (status == null) {
            throw new Exception("Statut invalide.");
        }
        if (employer == null) {
            throw new Exception("Employer invalide.");
        }

        HistoriqueDemandeStatus hist = new HistoriqueDemandeStatus();
        hist.setDemande(demande);
        hist.setStatus(status);
        hist.setEmployer(employer);
        hist.setMotif(motif);
        hist.setDate(LocalDate.now());

        historiqueDemandeStatusRepository.save(hist);
    }

    public Status getStatusBySigle(String sigle) {
        return statusRepository.findBySigle(sigle);
    }
}