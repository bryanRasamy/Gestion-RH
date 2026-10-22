package gestion.entreprise.service;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.HistoriqueDemandeStatus;
import gestion.entreprise.modele.Status;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import gestion.entreprise.repository.StatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Creation d'une demande de recrutement par un manager.
 *
 * <p>La creation pose le statut initial {@code cree} dans l'historique, en
 * meme transaction que la demande et ses lignes.</p>
 */
@Service
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailDemandeRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final StatusRepository statusRepository;
    private final DiplomeService diplomeService;

    public DemandeService(DemandeRepository demandeRepository,
                          DetailDemandeRepository detailDemandeRepository,
                          HistoriqueDemandeStatusRepository historiqueRepository,
                          StatusRepository statusRepository,
                          DiplomeService diplomeService) {
        this.demandeRepository = demandeRepository;
        this.detailDemandeRepository = detailDemandeRepository;
        this.historiqueRepository = historiqueRepository;
        this.statusRepository = statusRepository;
        this.diplomeService = diplomeService;
    }

    /**
     * Cree une demande et ses lignes de detail.
     *
     * @param idEmploye manager a l'origine de la demande
     * @param dateLimite echeance de la demande
     * @param lignes     lignes de detail ({@code idPoste}, {@code idDepartement},
     *                   {@code nbrPersonnel}, {@code description},
     *                   {@code idDiplome}, {@code anneeExperience})
     */
    @Transactional
    public Demande creerDemande(int idEmploye, LocalDate dateLimite, List<Map<String, Object>> lignes) {
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("Au moins une ligne est requise.");
        }

        Demande demande = new Demande();
        demande.setMatricule(genererMatricule());
        demande.setIdEmployer(idEmploye);
        demande.setDateLimite(dateLimite);
        Demande saved = demandeRepository.save(demande);

        int diplomeParDefaut = diplomeService.idDiplomeParDefaut();
        for (Map<String, Object> ligne : lignes) {
            DetailDemande detail = new DetailDemande();
            detail.setIdDemande(saved.getId());
            detail.setIdPoste(entier(ligne, "idPoste"));
            detail.setIdDepartement(entier(ligne, "idDepartement"));
            detail.setNbrPersonnel(entier(ligne, "nbrPersonnel"));
            detail.setDescription(texte(ligne, "description", "motif"));
            detail.setIdDiplome(valeur(ligne, "idDiplome", diplomeParDefaut));
            detail.setAnneeExperience(valeur(ligne, "anneeExperience", 0));
            detailDemandeRepository.save(detail);
        }

        Status statutInitial = statusRepository.findByLibelleIgnoreCase("cree");
        if (statutInitial == null) {
            throw new IllegalStateException("Le statut 'cree' est absent de la base.");
        }

        HistoriqueDemandeStatus historique = new HistoriqueDemandeStatus();
        historique.setIdDemande(saved.getId());
        historique.setStatus(statutInitial);
        historique.setIdEmploye(idEmploye);
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

    private int entier(Map<String, Object> ligne, String cle) {
        Object valeur = ligne.get(cle);
        if (!(valeur instanceof Number)) {
            throw new IllegalArgumentException("Le champ " + cle + " est obligatoire.");
        }
        return ((Number) valeur).intValue();
    }

    private int valeur(Map<String, Object> ligne, String cle, int defaut) {
        Object brut = ligne.get(cle);
        return brut instanceof Number ? ((Number) brut).intValue() : defaut;
    }

    private String texte(Map<String, Object> ligne, String cle, String cleAlternative) {
        Object valeur = ligne.get(cle);
        if (valeur == null) {
            valeur = ligne.get(cleAlternative);
        }
        return valeur == null ? "" : valeur.toString();
    }
}
