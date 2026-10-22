package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class RhCandidatureController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private DiplomeRepository diplomeRepository;
    @Autowired private CandidatureWorkflowService workflowService;
    @Autowired private TestTechniqueService testTechniqueService;

    @GetMapping("/rh/candidatures")
    public String liste(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);

            List<Candidat> candidats = candidatRepository.findAll();
            for (Candidat c : candidats) {
                c.setProchainNiveau(testTechniqueService.getProchainNiveau(c));
            }
            model.addAttribute("candidatures", candidats);
            model.addAttribute("titre", "Candidatures");
            model.addAttribute("menu", "candidatures");
            return "demandes/candidatures";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/rh/candidatures/{id}")
    public String detail(@PathVariable int id, Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);

            Candidat c = candidatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Candidature introuvable"));

            // Diplôme lisible
            Diplome diplome = null;
            if (c.getIdDiplome() != null) {
                diplome = diplomeRepository.findById(c.getIdDiplome()).orElse(null);
            }

            model.addAttribute("candidat", c);
            model.addAttribute("diplome", diplome);
            model.addAttribute("historique", workflowService.getHistorique(id));
            model.addAttribute("passages", testTechniqueService.getPassages(id));
            model.addAttribute("niveauMax", testTechniqueService.getNiveauMaxValide(c));
            model.addAttribute("role", u.getRole());
            model.addAttribute("utilisateur", u);
            model.addAttribute("titre", "Candidature " + c.getNom());
            return "demandes/candidature-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/candidatures";
        }
    }

    /* =========================================================
       Actions RH
       ========================================================= */

    /** Démarrer le test : EN_ATTENTE → EN_TEST */
    @PostMapping("/rh/candidatures/{id}/demarrer-test")
    public String demarrerTest(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            Candidat c = candidatRepository.findById(id).orElseThrow();
            if (!"EN_ATTENTE".equals(c.getStatus()) && !"CV_VALIDE".equals(c.getStatus())) {
                throw new RuntimeException("Impossible de démarrer un test dans ce statut.");
            }
            workflowService.changerStatut(c, "EN_TEST", u.getId(), "Test technique démarré par RH");
            ra.addFlashAttribute("succes", "Test technique démarré. Le candidat peut maintenant passer les niveaux.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/candidatures/" + id;
    }

    /** Passer à l'entretien : TESTS_REUSSIS → EN_ENTRETIEN */
    @PostMapping("/rh/candidatures/{id}/passer-entretien")
    public String passerEntretien(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            Candidat c = candidatRepository.findById(id).orElseThrow();
            if (!"TESTS_REUSSIS".equals(c.getStatus())) {
                throw new RuntimeException("Les tests doivent être réussis avant de passer à l'entretien.");
            }
            workflowService.changerStatut(c, "EN_ENTRETIEN", u.getId(), "Passage à l'entretien");
            ra.addFlashAttribute("succes", "Candidat convoqué à l'entretien.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/candidatures/" + id;
    }

    /** Accepter (après entretien) : EN_ENTRETIEN → ACCEPTE */
    @PostMapping("/rh/candidatures/{id}/accepter")
    public String accepter(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            Candidat c = candidatRepository.findById(id).orElseThrow();
            if (!"EN_ENTRETIEN".equals(c.getStatus())) {
                throw new RuntimeException("Le candidat doit être en entretien pour être accepté.");
            }
            workflowService.changerStatut(c, "ACCEPTE", u.getId(), "Candidat retenu après entretien");
            ra.addFlashAttribute("succes", "Candidat accepté.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/candidatures/" + id;
    }

    /** Recaler : à n'importe quel stade (sauf terminal) → REFUSE */
    @PostMapping("/rh/candidatures/{id}/recaler")
    public String recaler(@PathVariable int id,
                          @RequestParam(required = false) String motif,
                          HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            Candidat c = candidatRepository.findById(id).orElseThrow();

            if ("ACCEPTE".equals(c.getStatus()) || "REFUSE".equals(c.getStatus())) {
                throw new RuntimeException("Cette candidature est déjà clôturée.");
            }

            String m = (motif != null && !motif.isBlank())
                    ? motif.trim()
                    : "Candidature recalée par la RH";
            workflowService.changerStatut(c, "REFUSE", u.getId(), m);
            ra.addFlashAttribute("succes", "Candidature recalée.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/candidatures/" + id;
    }

    private void verifierRh(Utilisateur u) throws Exception {
        if (u == null || u.getRole() == null || !"RH".equalsIgnoreCase(u.getRole().getSigle())) {
            throw new Exception("Accès réservé aux RH");
        }
    }
}