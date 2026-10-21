package gestion.entreprise.controller;

import gestion.entreprise.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AccueilController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/accueil")
    public String accueil(@SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                          Model model, RedirectAttributes redirectAttributes) {
        try {
            int id = roleService.convertirId(idUtilisateur);
            roleService.verifierRole(id, "NORMAL");
            model.addAttribute("message", "Bienvenue dans votre espace personnel.");
            return "accueil";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }
}
