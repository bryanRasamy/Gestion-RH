package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DemandeService {

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private HistoriqueDemandeStatusRepository historiqueRepository;

    @Autowired
    private GrilleBudgetRepository grilleBudgetRepository;

    @Autowired
    private HistoriqueBudgetAnnuelRepository historiqueBudgetAnnuelRepository;

    @Transactional
    public Demande save(Demande demande) throws Exception {

        if (demande.getDateLimite() == null) {
            throw new Exception("La date limite est obligatoire.");
        }

        if (demande.getDateLimite().isBefore(LocalDate.now())) {
            throw new Exception("La date limite ne peut pas être dans le passé.");
        }

        demande.setMatricule(genererMatricule());

        Demande saved = demandeRepository.save(demande);

        List<DetailDemande> detailDemandes = demande.getdetailDemandes();

        if (detailDemandes == null || detailDemandes.isEmpty()) {
            throw new Exception("La demande doit contenir au moins un detail.");
        }

        for (DetailDemande detailDemande : detailDemandes) {
            if (detailDemande.getNbrPersonnel() <= 0) {
                throw new Exception("Le nombre de personnel doit être strictement positif.");
            }
            if (detailDemande.getAnneeExperience() < 0) {
                throw new Exception("L'annee d'expérience ne peut pas être négative.");
            }
            detailDemande.setDemande(saved);
            detailDemandeRepository.save(detailDemande);
        }

        Status status = statusRepository.findBySigle("CR");

        if (status == null) {
            throw new Exception("Status 'Creer' introuvable.");
        }

        HistoriqueDemandeStatus hist = new HistoriqueDemandeStatus();
        hist.setDemande(saved);
        hist.setEmployer(saved.getEmployer());
        hist.setStatus(status);
        hist.setDate(LocalDate.now());

        historiqueRepository.save(hist);

        return saved;
    }

    public Demande getById(int id) throws Exception {
        Demande demande = demandeRepository.findById(id).orElse(null);
        if (demande == null) {
            throw new Exception("Demande introuvable.");
        }
        return demande;
    }

    public List<Demande> getByEmployer(int idEmployer) {
        return demandeRepository.findByEmployerId(idEmployer);
    }

    public List<Demande> getByCurrentStatus(int idStatus) {
        return demandeRepository.findByCurrentStatus(idStatus);
    }

    /*
     * getMontant pour un detail: nbr_personnel * budget_max du poste pour ce poste
     * La grille est cherchée à la date de création de la demande.
     */
    public double getMontantDetailDemande(DetailDemande detail, LocalDate dateReference) throws Exception {
        if (detail.getNbrPersonnel() <= 0) {
            throw new Exception("Le nombre de personnel est invalide.");
        }

        GrilleBudget grille = grilleBudgetRepository.findActiveByPosteAndDate(detail.getPoste().getId(), dateReference);

        if (grille == null) {
            throw new Exception("Aucune grille budgétaire trouvée pour le poste: " + detail.getPoste().getLibelle());
        }

        if (grille.getBudgetMax() < 0) {
            throw new Exception("Le budget max de la grille est invalide.");
        }

        return detail.getNbrPersonnel() * grille.getBudgetMax();
    }

    /*
     * getMontant pour une demande: somme de getMontantDetailDemande pour chaque detail
     * La date de référence est la date de création (premier historique).
     */
    public double getMontantDemande(Demande demande) throws Exception {
        List<DetailDemande> details = detailDemandeRepository.findByDemandeId(demande.getId());

        HistoriqueDemandeStatus premier = historiqueRepository.findLatestByDemande(demande.getId());
        LocalDate dateCreation = (premier != null) ? premier.getDate() : LocalDate.now();

        double total = 0;
        for (DetailDemande detail : details) {
            total += getMontantDetailDemande(detail, dateCreation);
        }
        return total;
    }

    /*
     * respecteLeBudget: compare le montant de la demande avec le budget annuel actif
     * à la date de création de la demande.
     */
    public boolean respecteLeBudget(Demande demande) throws Exception {
        HistoriqueDemandeStatus premier = historiqueRepository.findLatestByDemande(demande.getId());
        LocalDate dateCreation = (premier != null) ? premier.getDate() : LocalDate.now();

        HistoriqueBudgetAnnuel budget = historiqueBudgetAnnuelRepository.findActiveAtDate(dateCreation);

        if (budget == null) {
            throw new Exception("Aucun budget annuel trouvé pour la date de la demande.");
        }

        if (budget.getValeur() < 0) {
            throw new Exception("Le budget annuel est invalide.");
        }

        double montantDemande = getMontantDemande(demande);
        return montantDemande <= budget.getValeur();
    }

    private String genererMatricule() {
        long total = demandeRepository.count() + 1;
        String matricule;
        do {
            matricule = String.format("DM%03d", total);
            total++;
        } while (demandeRepository.existsByMatricule(matricule));
        return matricule;
    }
}