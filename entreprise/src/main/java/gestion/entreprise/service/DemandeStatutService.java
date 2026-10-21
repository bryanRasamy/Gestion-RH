package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeStatutService {

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private HistoriqueDemandeStatusRepository historiqueRepository;

    @Autowired
    private OffreRepository offreRepository;

    public String statutCourant(int idDemande) {
        HistoriqueDemandeStatus historique = historiqueRepository.findFirstByDemande_IdOrderByIdDesc(idDemande);
        if (historique == null) {
            return "";
        }
        return historique.getStatus().getLibelle();
    }

    public List<Demande> listerParStatut(String statut) {
        List<Demande> demandes = demandeRepository.findAllByOrderByIdDesc();
        List<Demande> resultat = new ArrayList<>();
        for (Demande demande : demandes) {
            if (statut.equalsIgnoreCase(statutCourant(demande.getId()))) {
                resultat.add(demande);
            }
        }
        return resultat;
    }

    @Transactional
    public void changer(int idDemande, String nouveauStatut, int idEmployer, String motifRefus) {
        Demande demande = demandeRepository.verrouillerDemande(idDemande);
        if (demande == null) {
            throw new RuntimeException("Demande introuvable");
        }

        HistoriqueDemandeStatus precedent = historiqueRepository.findFirstByDemande_IdOrderByIdDesc(idDemande);
        if (precedent == null || !transitionAutorisee(precedent.getStatus().getLibelle(), nouveauStatut)) {
            throw new RuntimeException("Cette transition de statut n'est pas autorisée");
        }

        if ("refusé".equalsIgnoreCase(nouveauStatut) && (motifRefus == null || motifRefus.trim().isEmpty())) {
            throw new RuntimeException("Le motif du refus est obligatoire");
        }

        if ("validé DG".equalsIgnoreCase(nouveauStatut)) {
            creerOffres(demande);
        }

        enregistrerHistorique(demande, nouveauStatut, idEmployer, motifRefus);
    }

    private boolean transitionAutorisee(String precedent, String nouveau) {
        if ("créé".equalsIgnoreCase(precedent)) {
            return "validé RH".equalsIgnoreCase(nouveau) || "à revoir".equalsIgnoreCase(nouveau)
                || "refusé".equalsIgnoreCase(nouveau) || "expiré".equalsIgnoreCase(nouveau);
        }
        if ("validé RH".equalsIgnoreCase(precedent)) {
            return "validé Finance".equalsIgnoreCase(nouveau) || "à revoir".equalsIgnoreCase(nouveau)
                || "refusé".equalsIgnoreCase(nouveau) || "expiré".equalsIgnoreCase(nouveau);
        }
        if ("validé Finance".equalsIgnoreCase(precedent)) {
            return "validé DG".equalsIgnoreCase(nouveau) || "à revoir".equalsIgnoreCase(nouveau)
                || "refusé".equalsIgnoreCase(nouveau) || "expiré".equalsIgnoreCase(nouveau);
        }
        if ("à revoir".equalsIgnoreCase(precedent)) {
            return "validé RH".equalsIgnoreCase(nouveau) || "validé Finance".equalsIgnoreCase(nouveau)
                || "validé DG".equalsIgnoreCase(nouveau) || "abandonné".equalsIgnoreCase(nouveau)
                || "expiré".equalsIgnoreCase(nouveau);
        }
        return false;
    }

    private void creerOffres(Demande demande) {
        List<DetailDemande> details = detailDemandeRepository.findAllByDemande_Id(demande.getId());
        if (details.isEmpty()) {
            throw new RuntimeException("La demande ne contient aucun détail");
        }
        for (DetailDemande detail : details) {
            if (!offreRepository.existsByDetailDemande_Id(detail.getId())) {
                Offre offre = new Offre();
                offre.setDetailDemande(detail);
                offre.setDateLimite(demande.getDateLimite());
                offreRepository.save(offre);
            }
        }
    }

    @Transactional
    public void enregistrerHistorique(Demande demande, String libelle, int idEmployer, String motif) {
        Status status = statusRepository.findByLibelleIgnoreCase(libelle);
        if (status == null) {
            throw new RuntimeException("Statut inconnu : " + libelle);
        }

        HistoriqueDemandeStatus historique = new HistoriqueDemandeStatus();
        historique.setDemande(demande);
        historique.setStatus(status);
        historique.setDate(LocalDate.now());
        historique.setMotif(motif);
        if (idEmployer > 0) {
            Employer employer = new Employer();
            employer.setId(idEmployer);
            historique.setEmployer(employer);
        }
        historiqueRepository.save(historique);
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *", zone = "Indian/Antananarivo")
    public void expirerDemandesDepassees() {
        List<Demande> demandes = demandeRepository.findAllByOrderByIdDesc();
        for (Demande demandeListe : demandes) {
            Demande demande = demandeRepository.verrouillerDemande(demandeListe.getId());
            String statut = statutCourant(demande.getId());
            LocalDate limite = demande.getDateLimite();
            boolean actif = "créé".equalsIgnoreCase(statut) || "validé RH".equalsIgnoreCase(statut)
                || "validé Finance".equalsIgnoreCase(statut) || "à revoir".equalsIgnoreCase(statut);
            if (actif && limite != null && limite.isBefore(LocalDate.now())) {
                enregistrerHistorique(demande, "expiré", 0, null);
            }
        }
    }
}
