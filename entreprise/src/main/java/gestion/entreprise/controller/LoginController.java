package gestion.entreprise.controller;

import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.service.UtilisateurService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String afficherLogin(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "login";
    }

    @PostMapping("/login")
    public String seConnecter(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirectAttributes,
                              HttpSession session) {
        try {
            if (utilisateur.getEmail() == null || utilisateur.getEmail().isEmpty()) {
                throw new RuntimeException("L'email est obligatoire");
            }
            if (utilisateur.getMdp() == null || utilisateur.getMdp().isEmpty()) {
                throw new RuntimeException("Le mot de passe est obligatoire");
            }

            utilisateurService.findUtilisateur(utilisateur);

            if (utilisateur.getId() == 0) {
                throw new RuntimeException("L'utilisateur est introuvable");
            }

            utilisateur.setMdp(null);
            session.setAttribute("idUtilisateur", utilisateur.getId());
            session.setAttribute("idRole", utilisateur.getIdRole());

            return "redirect:" + pageAccueil(utilisateur);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String seDeconnecter(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "Vous etes deconnecte.");
        return "redirect:/login";
    }

    @GetMapping("/accueil")
    public String accueil() {
        return "accueil";
    }

    @GetMapping("/inscription")
    public String afficherInscription(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirectAttributes) {
        try {
            if (utilisateur.getEmail() == null || utilisateur.getEmail().isEmpty()) {
                throw new RuntimeException("L'email est obligatoire");
            }
            if (utilisateur.getMdp() == null || utilisateur.getMdp().isEmpty()) {
                throw new RuntimeException("Le mot de passe est obligatoire");
            }
            if (utilisateur.getMdp().length() < 8) {
                throw new RuntimeException("Le mot de passe trop court (8 caracteres minimum)");
            }

            try {
                utilisateurService.findUtilisateur(utilisateur);
            } catch (Exception e) {
                if (utilisateur.getId() > 0) {
                    throw new RuntimeException("L'utilisateur existe deja");
                }
                utilisateur.setId(0);
                utilisateur.setEmployer(null);
                utilisateur.setMdp(passwordEncoder.encode(utilisateur.getMdp()));
            }

            utilisateurService.saveUtilisateur(utilisateur);

            return "redirect:/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/inscription";
        }
    }

    /** Page d'accueil selon le role de l'utilisateur connecte. */
    private String pageAccueil(Utilisateur utilisateur) {
        TypeRole role = utilisateurService.role(utilisateur);
        if (role == null) {
            return "/accueil";
        }
        return role == TypeRole.MANAGER ? "/validation/manager/corrections" : "/validation";
    }
}
