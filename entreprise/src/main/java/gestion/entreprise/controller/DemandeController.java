package gestion.entreprise.controller;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.Poste;
import gestion.entreprise.service.DemandeService;
import gestion.entreprise.service.DemandeARevoirService;
import gestion.entreprise.service.RoleService;
import gestion.entreprise.repository.PosteRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DemandeController {

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private DemandeARevoirService revisionService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PosteRepository posteRepository;

    @GetMapping("/departements/accueil")
    public String accueilManager(@SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                 Model model, RedirectAttributes redirectAttributes) {
        try {
            int id = roleService.convertirId(idUtilisateur);
            boolean superAdmin = roleService.estSuperAdmin(id);
            roleService.verifierRole(id, "MANAGER");
            int idEmployer = roleService.employerConnecte(id);
            List<Demande> demandes = superAdmin ? demandeService.toutesLesDemandes()
                : demandeService.demandesDuManager(idEmployer);
            List<String> statuts = new java.util.ArrayList<>();
            for (Demande demande : demandes) {
                statuts.add(demandeService.statut(demande.getId()));
            }
            model.addAttribute("demandes", demandes);
            model.addAttribute("statuts", statuts);
            model.addAttribute("superAdmin", superAdmin);
            model.addAttribute("role", superAdmin ? "Super administrateur DG" : "Manager");
            return "demande/accueil";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/demandes/{id}")
    public String detailManager(@PathVariable int id,
                                @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                Model model, RedirectAttributes redirectAttributes) {
        try {
            int idUtilisateurConnecte = roleService.convertirId(idUtilisateur);
            boolean superAdmin = roleService.estSuperAdmin(idUtilisateurConnecte);
            roleService.verifierRole(idUtilisateurConnecte, "MANAGER");
            int idEmployer = roleService.employerConnecte(idUtilisateurConnecte);
            Demande demande = demandeService.demande(id);
            if (!superAdmin && demande.getEmployer().getId() != idEmployer) {
                throw new RuntimeException("Cette demande ne vous appartient pas");
            }

            model.addAttribute("demande", demande);
            model.addAttribute("details", demandeService.details(id));
            model.addAttribute("statut", demandeService.statut(id));
            model.addAttribute("role", "MANAGER");
            model.addAttribute("retour", "/departements/accueil");
            if ("à revoir".equalsIgnoreCase(demandeService.statut(id))) {
                model.addAttribute("corrections", revisionService.correctionsDemande(id, idEmployer, superAdmin));
                model.addAttribute("modeCorrection", true);
            }
            return "demande/detail";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/departements/accueil";
        }
    }

    @GetMapping("/demandes/nouvelle")
    public String nouvelleDemande(@SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                  Model model, RedirectAttributes redirectAttributes) {
        try {
            int id = roleService.convertirId(idUtilisateur);
            boolean superAdmin = roleService.estSuperAdmin(id);
            roleService.verifierRole(id, "MANAGER");
            chargerFormulaire(model);
            model.addAttribute("superAdmin", superAdmin);
            return "demande/nouvelle";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/demandes/creer")
    public String creerDemande(@SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                               @RequestParam(name = "dateLimite", required = false) String dateLimite,
                               @RequestParam(name = "posteIds", required = false) int[] postes,
                               @RequestParam(name = "departementIds", required = false) int[] departements,
                               @RequestParam(name = "nombres", required = false) int[] nombres,
                               @RequestParam(name = "descriptions", required = false) String[] descriptions,
                               Model model, RedirectAttributes redirectAttributes) {
        boolean superAdmin = false;
        try {
            int id = roleService.convertirId(idUtilisateur);
            superAdmin = roleService.estSuperAdmin(id);
            roleService.verifierRole(id, "MANAGER");
            LocalDate limite = dateLimite == null || dateLimite.isEmpty() ? null : LocalDate.parse(dateLimite);
            Demande demande = demandeService.creerDemande(roleService.employerConnecte(id), limite,
                postes, departements, nombres, descriptions);
            redirectAttributes.addFlashAttribute("succes", "Demande " + demande.getMatricule() + " créée");
            return "redirect:/departements/accueil";
        } catch (RuntimeException erreur) {
            model.addAttribute("erreur", erreur.getMessage());
            chargerFormulaire(model);
            model.addAttribute("superAdmin", superAdmin);
            return "demande/nouvelle";
        }
    }

    private void chargerFormulaire(Model model) {
        List<Poste> postes = posteRepository.findAll();
        model.addAttribute("postes", postes);
    }
}
