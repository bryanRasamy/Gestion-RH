package gestion.entreprise.controller;

import gestion.entreprise.config.UtilisateurActif;
import gestion.entreprise.dto.DemandeCarte;
import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.Employer;
import gestion.entreprise.modele.StatutDemande;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.EmployerRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import gestion.entreprise.service.UtilisateurService;
import gestion.entreprise.service.ValidationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/** Demandes de recrutement : creation et consultation. */
@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeRepository demandeRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final EmployerRepository employerRepository;
    private final ValidationService validationService;
    private final UtilisateurService utilisateurService;

    public DemandeController(DemandeRepository demandeRepository,
                             HistoriqueDemandeStatusRepository historiqueRepository,
                             EmployerRepository employerRepository,
                             ValidationService validationService,
                             UtilisateurService utilisateurService) {
        this.demandeRepository = demandeRepository;
        this.historiqueRepository = historiqueRepository;
        this.employerRepository = employerRepository;
        this.validationService = validationService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public String mesDemandes(@UtilisateurActif Utilisateur utilisateur, Model model) {
        int idEmploye = utilisateurService.idEmploye(utilisateur);
        List<DemandeCarte> demandes = new ArrayList<>();
        for (Demande demande : demandeRepository.findAll()) {
            if (demande.getIdEmployer() != idEmploye) {
                continue;
            }
            StatutDemande statut = validationService.statutCourant(demande.getId());
            if (statut == null) {
                continue;
            }
            var dernier = historiqueRepository.findTopByIdDemandeOrderByDateDescIdDesc(demande.getId());
            demandes.add(new DemandeCarte(
                    demande.getId(), demande.getMatricule(), demande.getIdEmployer(),
                    utilisateurService.nomAffiche(utilisateur), demande.getDateLimite(),
                    statut.getLibelle(), dernier != null ? dernier.getDate() : null,
                    dernier != null ? dernier.getMotif() : null,
                    null, null, null, null, validationService.lignes(demande.getId())));
        }
        model.addAttribute("demandes", demandes);
        model.addAttribute("role", utilisateurService.libelleRole(utilisateur));
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("page", "demandes");
        return "demande/liste";
    }

    @GetMapping("/nouvelle")
    public String nouvelleDemande(@UtilisateurActif Utilisateur utilisateur, Model model) {
        if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
            return "redirect:/validation";
        }
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("page", "nouvelle-demande");
        return "demande/nouvelle";
    }

    @GetMapping("/{id}")
    public String detail(@UtilisateurActif Utilisateur utilisateur, @PathVariable int id, Model model) {
        Demande demande = demandeRepository.findOneById(id);
        if (demande == null) {
            return "redirect:/demandes";
        }
        StatutDemande statut = validationService.statutCourant(id);
        Employer demandeur = employerRepository.findOneById(demande.getIdEmployer());

        model.addAttribute("demande", demande);
        model.addAttribute("demandeur", demandeur != null
                ? demandeur.getPrenom() + " " + demandeur.getNom() : "");
        model.addAttribute("lignes", validationService.lignes(id));
        model.addAttribute("historique", historiqueRepository.findByIdDemandeOrderByDateAsc(id));
        model.addAttribute("statut", statut != null ? statut.getLibelle() : "");
        model.addAttribute("role", utilisateurService.libelleRole(utilisateur));
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("page", "demandes");
        return "demande/detail";
    }
}
