package gestion.entreprise.controller;

import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.service.RoleService;
import gestion.entreprise.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("idUtilisateur")
public class LoginController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String afficherLogin() {
        return "login";
    }

    @PostMapping("/logout")
    public String seDeconnecter(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String seConnecter(@RequestParam(name = "email", required = false) String email,
                              @RequestParam(name = "mdp", required = false) String mdp,
                              Model model, RedirectAttributes redirectAttributes) {
        try {
            if (email == null || email.trim().isEmpty()) {
                throw new RuntimeException("L'email est obligatoire");
            }
            if (mdp == null || mdp.isEmpty()) {
                throw new RuntimeException("Le mot de passe est obligatoire");
            }
            Utilisateur utilisateur = utilisateurService.authentifier(email, mdp);
            model.addAttribute("idUtilisateur", String.valueOf(utilisateur.getId()));

            String role = roleService.roleUtilisateur(utilisateur.getId());
            if ("RH".equals(role)) {
                return "redirect:/rh/accueil";
            }
            if ("DG".equals(role)) {
                return "redirect:/dg/accueil";
            }
            if ("FINANCE".equals(role)) {
                return "redirect:/finance/accueil";
            }
            if ("MANAGER".equals(role)) {
                return "redirect:/departements/accueil";
            }
            return "redirect:/accueil";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/inscription")
    public String afficherInscription() {
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@RequestParam(name = "email", required = false) String email,
                            @RequestParam(name = "mdp", required = false) String mdp,
                            Model model, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.inscrire(email, mdp);
            model.addAttribute("idUtilisateur", String.valueOf(utilisateur.getId()));
            return "redirect:/accueil";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/inscription";
        }
    }
}
