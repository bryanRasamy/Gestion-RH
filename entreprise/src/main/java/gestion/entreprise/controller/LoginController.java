package gestion.entreprise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public String seConnecter(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirectAttributes, HttpSession session) {
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

            Role role = utilisateur.getRole();

            if (role == null) {
                // Candidat → espace offres
                return "redirect:/offres";
            }

            return "redirect:/" + role.getSigle().toLowerCase() + "/accueil";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/inscription")
    public String afficherInscription(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "inscription";
    }

    @PostMapping("/inscription")
    public String inscrire(@ModelAttribute Utilisateur utilisateur, RedirectAttributes redirectAttributes, HttpSession session) {
        try {
            if (utilisateur.getEmail() == null || utilisateur.getEmail().isEmpty()) {
                throw new RuntimeException("L'email est obligatoire");
            }

            if (utilisateur.getMdp() == null || utilisateur.getMdp().isEmpty()) {
                throw new RuntimeException("Le mot de passe est obligatoire");
            }

            if (utilisateur.getMdp().length() < 8) {
                throw new RuntimeException("Le mot de passe trop court (8 caractères minimum)");
            }

            // Vérification d'unicité de l'email (sans dépendre d'une exception)
            if (utilisateurService.emailExiste(utilisateur.getEmail())) {
                throw new RuntimeException("Un compte existe déjà avec cet e-mail");
            }

            // Forcer un nouvel utilisateur (pas d'id, pas d'employer, pas de rôle)
            utilisateur.setId(0);
            utilisateur.setEmployer(null);
            utilisateur.setRole(null);
            utilisateur.setMdp(passwordEncoder.encode(utilisateur.getMdp()));

            utilisateurService.saveUtilisateur(utilisateur);

            session.setAttribute("idUtilisateur", utilisateur.getId());

            // Nouvel inscrit = candidat → espace offres
            return "redirect:/offres";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/inscription";
        }
    }

    @GetMapping("/logout")
    public String seDeconnecter(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}