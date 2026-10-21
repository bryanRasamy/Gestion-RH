package gestion.entreprise.controller;

import gestion.entreprise.service.DemandeARevoirService;
import gestion.entreprise.service.DemandeService;
import gestion.entreprise.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CorrectionController {

    @Autowired
    private DemandeARevoirService revisionService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/correction/accueil")
    public String accueil(@SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                          Model model, RedirectAttributes redirectAttributes) {
        try {
            int id = roleService.convertirId(idUtilisateur);
            roleService.verifierRole(id, "MANAGER");
            model.addAttribute("corrections", revisionService.correctionsManager(roleService.employerConnecte(id)));
            return "demande/correction";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/correction/demande/{id}")
    public String detailCorrection(@PathVariable int id,
                                   @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                   Model model, RedirectAttributes redirectAttributes) {
        try {
            int idUtilisateurConnecte = roleService.convertirId(idUtilisateur);
            roleService.verifierRole(idUtilisateurConnecte, "MANAGER");
            int idEmployer = roleService.employerConnecte(idUtilisateurConnecte);
            model.addAttribute("demande", demandeService.demande(id));
            model.addAttribute("details", demandeService.details(id));
            model.addAttribute("statut", demandeService.statut(id));
            model.addAttribute("corrections", revisionService.correctionsDemande(id, idEmployer));
            model.addAttribute("role", "MANAGER");
            model.addAttribute("modeCorrection", true);
            model.addAttribute("retour", "/correction/accueil");
            return "demande/detail";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/correction/accueil";
        }
    }

    @PostMapping("/correction/demande/{id}/accepter")
    public String accepter(@PathVariable int id,
                           @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                           RedirectAttributes redirectAttributes) {
        try {
            int idUser = roleService.convertirId(idUtilisateur);
            roleService.verifierRole(idUser, "MANAGER");
            revisionService.accepterCorrection(id, roleService.employerConnecte(idUser));
            redirectAttributes.addFlashAttribute("succes", "La correction a été appliquée");
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
        }
        return "redirect:/correction/accueil";
    }

    @PostMapping("/correction/demande/{id}/abandonner")
    public String abandonner(@PathVariable int id,
                             @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                             RedirectAttributes redirectAttributes) {
        try {
            int idUser = roleService.convertirId(idUtilisateur);
            roleService.verifierRole(idUser, "MANAGER");
            revisionService.abandonner(id, roleService.employerConnecte(idUser));
            redirectAttributes.addFlashAttribute("succes", "La demande a été abandonnée");
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
        }
        return "redirect:/correction/accueil";
    }
}
