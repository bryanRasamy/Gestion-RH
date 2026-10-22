package gestion.entreprise.controller.candidat;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.CandidatRepository;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class MesCandidaturesController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private TestTechniqueService testTechniqueService;
    @Autowired private CandidatureWorkflowService workflowService;

    @GetMapping("/mes-candidatures")
    public String liste(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Object idU = session.getAttribute("idUtilisateur");
            if (idU == null) throw new RuntimeException("Veuillez vous connecter");

            Utilisateur utilisateur = utilisateurService.getById((int) idU);
            if (utilisateur == null) throw new RuntimeException("Utilisateur introuvable");
            if (utilisateur.getEmployer() != null) throw new RuntimeException("Espace candidat uniquement");

            List<Candidat> candidats = candidatRepository.findByUtilisateurId(utilisateur.getId());

            // Pour chaque candidat, ajouter le prochain niveau disponible
            for (Candidat c : candidats) {
                int prochain = testTechniqueService.getProchainNiveau(c);
                c.setProchainNiveau(prochain); // @Transient à ajouter
            }

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("candidatures", candidats);
            model.addAttribute("titre", "Mes candidatures");
            model.addAttribute("menu", "mes-candidatures");
            model.addAttribute("page", "candidat/mes-candidatures");
            return "candidat/header";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }
}