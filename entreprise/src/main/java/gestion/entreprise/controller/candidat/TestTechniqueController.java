package gestion.entreprise.controller.candidat;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.TestTechniqueQuestionRepository;
import gestion.entreprise.repository.CandidatRepository;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class TestTechniqueController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private TestTechniqueService testTechniqueService;
    @Autowired private TestTechniqueQuestionRepository questionRepository;

    @GetMapping("/mes-candidatures/{idCandidat}/test/{niveau}")
    public String afficherTest(@PathVariable int idCandidat, @PathVariable int niveau,
                               Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Candidat candidat = verifierAcces(idCandidat, session);
            int prochain = testTechniqueService.getProchainNiveau(candidat);
            if (prochain != niveau) throw new RuntimeException("Ce niveau n'est pas accessible.");

            TestTechnique test = testTechniqueService.getTest(idCandidat, niveau);
            List<TestTechniqueQuestion> questions = questionRepository
                    .findByTestTechniqueIdAndActifTrueOrderByOrdre(test.getId());

            model.addAttribute("utilisateur", candidat.getUtilisateur());
            model.addAttribute("candidat", candidat);
            model.addAttribute("test", test);
            model.addAttribute("questions", questions);
            model.addAttribute("niveau", niveau);
            model.addAttribute("titre", "Test niveau " + niveau);
            model.addAttribute("menu", "mes-candidatures");
            model.addAttribute("page", "candidat/test-technique");
            return "candidat/header";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/test/{niveau}")
    public String soumettre(@PathVariable int idCandidat, @PathVariable int niveau,
                            @RequestParam Map<String, String> params,
                            HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            int idUtilisateur = (int) session.getAttribute("idUtilisateur");

            Map<Integer, String> reponses = new HashMap<>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (e.getKey().startsWith("q_")) {
                    try {
                        reponses.put(Integer.parseInt(e.getKey().substring(2)), e.getValue());
                    } catch (NumberFormatException ignored) {}
                }
            }

            TestTechniquePassage passage = testTechniqueService
                    .soumettre(idCandidat, niveau, reponses, idUtilisateur);

            if ("VALIDE".equals(passage.getStatut())) {
                ra.addFlashAttribute("succes",
                        "Test niveau " + niveau + " réussi (" + passage.getScore() + "%)");
            } else {
                ra.addFlashAttribute("erreur",
                        "Test niveau " + niveau + " échoué (" + passage.getScore() + "%).");
            }
            return "redirect:/mes-candidatures";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures/" + idCandidat + "/test/" + niveau;
        }
    }

    private Candidat verifierAcces(int idCandidat, HttpSession session) throws Exception {
        Object idU = session.getAttribute("idUtilisateur");
        if (idU == null) throw new RuntimeException("Veuillez vous connecter");
        Candidat c = candidatRepository.findById(idCandidat)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        if (c.getUtilisateur() == null || c.getUtilisateur().getId() != (int) idU) {
            throw new RuntimeException("Accès non autorisé");
        }
        return c;
    }
}