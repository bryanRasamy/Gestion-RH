package gestion.entreprise.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.vue.*;

import java.time.LocalDateTime;

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

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    @Autowired
    private DemandeARevoirRepository demandeARevoirRepository;

    public List<HistoriqueDemandeStatusView> getDemandesRecent() {
        return enrichir(historiqueDemandeStatusViewRepository.findLatestStatusPerDemand());
    }

    public List<HistoriqueDemandeStatusView> getDemandesByEmployer(int idEmployer) {
        return enrichir(historiqueDemandeStatusViewRepository.findLatestStatusPerDemandByEmployer(idEmployer));
    }

    public List<HistoriqueDemandeStatusView> getDemandesByStatusAndRole(String sigleStatus, String sigleRole) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findByRoleSigle(sigleRole);

        if (utilisateurs == null || utilisateurs.isEmpty()) {
            return new ArrayList<>();
        }

        int[] evaluateurIds = new int[utilisateurs.size()];
        for (int i = 0; i < utilisateurs.size(); i++) {
            evaluateurIds[i] = utilisateurs.get(i).getEmployer().getId();
        }

        return enrichir(historiqueDemandeStatusViewRepository
                .findLatestByStatusAndEvaluateurs(sigleStatus, evaluateurIds));
    }

    public List<HistoriqueDemandeStatusView> getByDemande(int idDemande) {
        return enrichir(historiqueDemandeStatusViewRepository.findAllByDemande(idDemande));
    }

    /**
     * Retourne la dernière ligne d'historique (par id) pour une demande.
     * Utile pour identifier le correcteur courant (auteur du dernier AR).
     */
    public HistoriqueDemandeStatus getDernierStatutHistorique(int idDemande) {
        return historiqueDemandeStatusRepository.findLatestByDemande(idDemande);
    }

    private List<HistoriqueDemandeStatusView> enrichir(List<HistoriqueDemandeStatusView> vues) {
        if (vues == null || vues.isEmpty()) return vues;

        Map<Integer, String> roleParEmployer = new HashMap<>();

        for (HistoriqueDemandeStatusView v : vues) {
            int idEmp = v.getIdEmployerEvaluateur();
            if (idEmp <= 0) continue;

            if (!roleParEmployer.containsKey(idEmp)) {
                Utilisateur u = utilisateurRepository.findFirstByEmployerId(idEmp);
                String sigle = (u != null && u.getRole() != null) ? u.getRole().getSigle() : null;
                roleParEmployer.put(idEmp, sigle);
            }
            v.setEvaluateurRole(roleParEmployer.get(idEmp));
        }
        return vues;
    }

    public List<HistoriqueDemandeStatusView> filtrer(List<HistoriqueDemandeStatusView> demandes,
            String statusSigle, LocalDateTime dateMin, LocalDateTime dateMax) {

        List<HistoriqueDemandeStatusView> resultat = new ArrayList<>();

        for (HistoriqueDemandeStatusView d : demandes) {
            boolean garder = true;

            if (statusSigle != null && !statusSigle.isEmpty()) {
                if (!d.getSigle().equalsIgnoreCase(statusSigle)) {
                    garder = false;
                }
            }

            if (dateMin != null && d.getDate().isBefore(dateMin)) {
                garder = false;
            }

            if (dateMax != null && d.getDate().isAfter(dateMax)) {
                garder = false;
            }

            if (garder) resultat.add(d);
        }

        return resultat;
    }

    @Transactional
    public void changerStatut(Demande demande, Status status, Employer employer, String motif) throws Exception {
        if (demande == null) throw new Exception("Demande invalide.");
        if (status == null) throw new Exception("Statut invalide.");
        if (employer == null) throw new Exception("Employer invalide.");

        boolean dejaPresent = historiqueDemandeStatusRepository
                .existsByDemandeIdAndStatusIdAndEmployerId(
                        demande.getId(), status.getId(), employer.getId());
        if (dejaPresent) {
            throw new Exception("Cette action a déjà été effectuée par cet utilisateur.");
        }

        HistoriqueDemandeStatus hist = new HistoriqueDemandeStatus();
        hist.setDemande(demande);
        hist.setStatus(status);
        hist.setEmployer(employer);
        hist.setMotif(motif);
        hist.setDate(LocalDateTime.now());

        historiqueDemandeStatusRepository.save(hist);
    }

    /**
     * Valide une demande en statut "AR" (à revoir).
     *
     * - L'évaluateur (id_employer) dans le nouvel historique VA est le CORRECTEUR
     *   (celui qui a posé l'AR), pas le manager. Le manager ne fait que confirmer.
     * - L'effectif de chaque DetailDemande est mis à jour avec la valeur corrigée
     *   (nbSouhaite de la correction soumise par le correcteur).
     * - Un snapshot est inséré dans demande_a_revoir pour traçabilité.
     */
    @Transactional
    public void validerCorrection(Demande demande, Employer manager, String motif) throws Exception {
        if (demande == null) throw new Exception("Demande invalide.");
        if (manager == null) throw new Exception("Manager invalide.");

        HistoriqueDemandeStatus histDernier = historiqueDemandeStatusRepository
                .findLatestByDemande(demande.getId());

        if (histDernier == null || histDernier.getStatus() == null
                || !"AR".equalsIgnoreCase(histDernier.getStatus().getSigle())) {
            throw new Exception("Cette demande n'est pas en attente de correction.");
        }

        Employer correcteur = histDernier.getEmployer();
        if (correcteur == null) {
            throw new Exception("Impossible de retrouver le correcteur de la demande.");
        }

        Status statusVA = statusRepository.findBySigle("VA");
        if (statusVA == null) throw new Exception("Statut 'Valider' introuvable.");

        boolean dejaValide = historiqueDemandeStatusRepository
                .existsByDemandeIdAndStatusIdAndEmployerId(
                        demande.getId(), statusVA.getId(), correcteur.getId());
        if (dejaValide) {
            throw new Exception("Cette correction a déjà été validée.");
        }

        List<DemandeARevoir> corrections = demandeARevoirRepository
                .findByDemandeDetailsDemandeId(demande.getId());

        List<DetailDemande> details = detailDemandeRepository.findByDemandeId(demande.getId());
        if (details == null || details.isEmpty()) {
            throw new Exception("Aucun détail à valider.");
        }

        for (DetailDemande detail : details) {
            if (detail.getNbrPersonnel() <= 0) {
                throw new Exception("Un effectif est invalide (≤ 0).");
            }

            // Correction du correcteur courant pour ce détail (la plus récente)
            DemandeARevoir correctionDetail = null;
            for (DemandeARevoir c : corrections) {
                if (c.getDemandeur() != null
                        && c.getDemandeur().getId() == correcteur.getId()
                        && c.getDemandeDetails().getId() == detail.getId()) {
                    if (correctionDetail == null || c.getId() > correctionDetail.getId()) {
                        correctionDetail = c;
                    }
                }
            }

            int effectifFinal = (correctionDetail != null)
                    ? correctionDetail.getNbSouhaite()
                    : detail.getNbrPersonnel();

            if (effectifFinal <= 0) {
                throw new Exception("Un effectif corrigé est invalide (≤ 0).");
            }

            // Snapshot de traçabilité
            DemandeARevoir snapshot = new DemandeARevoir();
            snapshot.setDemandeur(correcteur);
            snapshot.setDemandeDetails(detail);
            snapshot.setNbSouhaite(effectifFinal);
            demandeARevoirRepository.save(snapshot);

            // ★ Mise à jour de la demande réelle
            detail.setNbrPersonnel(effectifFinal);
            detailDemandeRepository.save(detail);
        }

        // Pose du statut VA — évaluateur = correcteur
        HistoriqueDemandeStatus hist = new HistoriqueDemandeStatus();
        hist.setDemande(demande);
        hist.setStatus(statusVA);
        hist.setEmployer(correcteur);
        hist.setMotif(motif != null && !motif.isEmpty() ? motif : "Correction acceptée par le manager.");
        hist.setDate(LocalDateTime.now());

        historiqueDemandeStatusRepository.save(hist);
    }

    public Status getStatusBySigle(String sigle) {
        return statusRepository.findBySigle(sigle);
    }
}