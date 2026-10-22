package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        if (demande.getDateLimite().isBefore(LocalDateTime.now())) {
            throw new Exception("La date limite ne peut pas être dans le passé.");
        }

        demande.setMatricule(genererMatricule());

        Demande saved = demandeRepository.save(demande);

        List<DetailDemande> detailDemandes = demande.getdetailDemandes();

        if (detailDemandes == null || detailDemandes.isEmpty()) {
            throw new Exception("La demande doit contenir au moins un détail.");
        }

        for (DetailDemande detailDemande : detailDemandes) {
            if (detailDemande.getNbrPersonnel() <= 0) {
                throw new Exception("Le nombre de personnel doit être strictement positif.");
            }
            if (detailDemande.getAnneeExperience() < 0) {
                throw new Exception("L'année d'expérience ne peut pas être négative.");
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
        hist.setDate(LocalDateTime.now());

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

    /**
     * Vérifie que la demande n'est pas ABANDONNÉE (AN).
     * Une demande annulée est définitivement clôturée : aucune transition possible.
     *
     * Les statuts VA (validée) et RF (refusée) ne bloquent PAS : le workflow
     * multi-niveaux (RH → Finance → DG) peut encore faire évoluer la demande.
     *
     * @throws Exception si le dernier statut est AN
     */
    public void verifierNonCloturee(int idDemande) throws Exception {
        HistoriqueDemandeStatus dernier = historiqueRepository.findLatestByDemande(idDemande);
        if (dernier == null || dernier.getStatus() == null) {
            return;
        }

        String sigle = dernier.getStatus().getSigle();
        if ("AN".equalsIgnoreCase(sigle)) {
            throw new Exception("Cette demande a été abandonnée. Aucune action n'est possible.");
        }
    }

    /**
     * Montant d'un détail : nbr_personnel × budget_max du poste à la date de référence.
     */
    public double getMontantDetailDemande(DetailDemande detail, LocalDateTime dateReference) throws Exception {
        if (detail.getNbrPersonnel() <= 0) {
            throw new Exception("Le nombre de personnel est invalide.");
        }

        GrilleBudget grille = grilleBudgetRepository.findActiveByPosteAndDate(detail.getPoste().getId(), dateReference);

        if (grille == null) {
            throw new Exception("Aucune grille budgétaire trouvée pour le poste : " + detail.getPoste().getLibelle());
        }

        if (grille.getBudgetMax() < 0) {
            throw new Exception("Le budget max de la grille est invalide.");
        }

        return detail.getNbrPersonnel() * grille.getBudgetMax();
    }

    /**
     * Montant total d'une demande calculé à sa DATE DE CRÉATION
     * (première ligne d'historique), et non à la date du dernier changement.
     */
    public double getMontantDemande(Demande demande) throws Exception {
        List<DetailDemande> details = detailDemandeRepository.findByDemandeId(demande.getId());

        HistoriqueDemandeStatus premiere = historiqueRepository.findFirstByDemande(demande.getId());
        LocalDateTime dateCreation = (premiere != null) ? premiere.getDate() : LocalDateTime.now();

        double total = 0;
        for (DetailDemande detail : details) {
            total += getMontantDetailDemande(detail, dateCreation);
        }
        return total;
    }

    /**
     * Compare le montant de la demande (calculé à sa date de création)
     * au budget annuel actif à cette même date.
     */
    public boolean respecteLeBudget(Demande demande) throws Exception {
        HistoriqueDemandeStatus premiere = historiqueRepository.findFirstByDemande(demande.getId());
        LocalDateTime dateCreation = (premiere != null) ? premiere.getDate() : LocalDateTime.now();

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