package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeService {

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DetailDemandeRepository detailRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PosteRepository posteRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private DemandeStatutService statutService;

    @Transactional
    public Demande creerDemande(int idEmployer, LocalDate dateLimite, int[] postes,
                                 int[] departements, int[] nombres, String[] descriptions) {
        if (postes == null || postes.length == 0 || departements == null || nombres == null || descriptions == null
            || postes.length != departements.length || postes.length != nombres.length || postes.length != descriptions.length) {
            throw new RuntimeException("Les lignes de la demande sont incomplètes");
        }
        if (dateLimite == null || dateLimite.isBefore(LocalDate.now())) {
            throw new RuntimeException("La date limite doit être aujourd'hui ou dans le futur");
        }

        Employer employer = employerRepository.getReferenceById(idEmployer);
        Demande demande = new Demande();
        demande.setEmployer(employer);
        demande.setDateLimite(dateLimite);
        demande.setMatricule("DEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        demande = demandeRepository.save(demande);

        for (int index = 0; index < postes.length; index++) {
            int nombre = nombres[index];
            if (nombre < 1) {
                throw new RuntimeException("Le nombre de personnes doit être supérieur à zéro");
            }
            Poste poste = posteRepository.findFirstById(postes[index]);
            if (poste == null || poste.getDepartement().getId() != departements[index]) {
                throw new RuntimeException("Le poste choisi ne correspond pas au département");
            }
            DetailDemande detail = new DetailDemande();
            detail.setDemande(demande);
            detail.setPoste(poste);
            detail.setDepartement(departementRepository.getReferenceById(departements[index]));
            detail.setNbrPersonnel(nombre);
            detail.setDescription(descriptions[index]);
            detailRepository.save(detail);
        }
        statutService.enregistrerHistorique(demande, "créé", idEmployer, null);
        return demande;
    }

    public List<Demande> demandesDuManager(int idEmployer) {
        return demandeRepository.findAllByEmployer_IdOrderByIdDesc(idEmployer);
    }

    public Demande demande(int idDemande) {
        Demande demande = demandeRepository.findFirstById(idDemande);
        if (demande == null) {
            throw new RuntimeException("Demande introuvable");
        }
        return demande;
    }

    public List<DetailDemande> details(int idDemande) {
        return detailRepository.findAllByDemande_Id(idDemande);
    }

    public String statut(int idDemande) {
        return statutService.statutCourant(idDemande);
    }
}
