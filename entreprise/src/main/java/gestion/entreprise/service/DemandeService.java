package gestion.entreprise.service;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.HistoriqueDemandeStatus;
import gestion.entreprise.modele.Status;
import gestion.entreprise.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailDemandeRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final StatusRepository statusRepository;

    public DemandeService(DemandeRepository demandeRepository,
                          DetailDemandeRepository detailDemandeRepository,
                          HistoriqueDemandeStatusRepository historiqueRepository,
                          StatusRepository statusRepository) {
        this.demandeRepository = demandeRepository;
        this.detailDemandeRepository = detailDemandeRepository;
        this.historiqueRepository = historiqueRepository;
        this.statusRepository = statusRepository;
    }

    @Transactional
    public Demande creerDemande(int idEmployer, LocalDate dateLimite,
                                List<Map<String, Object>> lignes) {
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("Au moins une ligne est requise.");
        }

        // 1. Créer la demande
        Demande demande = new Demande();
        demande.setMatricule(genererMatricule());
        demande.setIdEmployer(idEmployer);
        demande.setDateLimite(dateLimite);
        Demande saved = demandeRepository.save(demande);

        // 2. Créer chaque ligne de détail
        for (Map<String, Object> ligne : lignes) {
            DetailDemande detail = new DetailDemande();
            detail.setIdDemande(saved.getId());
            detail.setIdPoste(((Number) ligne.get("idPoste")).intValue());
            detail.setIdDepartement(((Number) ligne.get("idDepartement")).intValue());
            detail.setNbrPersonnel(((Number) ligne.get("nbrPersonnel")).intValue());
            Object description = ligne.get("description");
            detail.setDescription(description != null ? description.toString() : (String) ligne.get("motif"));
            detailDemandeRepository.save(detail);
        }

        // 3. Initialiser le workflow (Manager -> statut créé)
        Status statutInitial = statusRepository.findByLibelleIgnoreCase("cree");
        if (statutInitial == null) {
            statutInitial = statusRepository.findByLibelleIgnoreCase("creer");
        }
        if (statutInitial == null) {
            throw new IllegalStateException("Le statut 'cree' est absent de la base.");
        }

        HistoriqueDemandeStatus historique = new HistoriqueDemandeStatus();
        historique.setIdDemande(saved.getId());
        historique.setLibelle(statutInitial.getLibelle());
        historique.setIdEmploye(idEmployer);
        historique.setDate(LocalDate.now());
        historiqueRepository.save(historique);

        return saved;
    }

    private String genererMatricule() {
        int annee = LocalDate.now().getYear();
        long total = demandeRepository.count() + 1;
        String matricule;
        do {
            matricule = String.format("REQ-%d-%03d", annee, total);
            total++;
        } while (demandeRepository.existsByMatricule(matricule));
        return matricule;
    }
}