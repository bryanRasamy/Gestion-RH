package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
public class RhPropositionController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private PropositionEmbaucheService propositionService;
    @Autowired private ContratPdfService contratPdfService;
    @Autowired private CandidatRepository candidatRepository;

    @GetMapping("/rh/propositions")
    public String liste(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);
            model.addAttribute("propositions", propositionService.getPropositions());
            model.addAttribute("titre", "Propositions d'embauche");
            return "demandes/propositions";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/rh/candidatures/{idCandidat}/proposition")
    public String creerFormulaire(@PathVariable int idCandidat, Model model,
                                   HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);
            Candidat c = candidatRepository.findById(idCandidat).orElseThrow();
            if (!"ACCEPTE".equals(c.getStatus())) {
                throw new RuntimeException("Le candidat doit être accepté avant de créer une proposition.");
            }
            model.addAttribute("candidat", c);
            model.addAttribute("titre", "Nouvelle proposition");
            return "demandes/proposition-form";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/candidatures/" + idCandidat;
        }
    }

    @PostMapping("/rh/candidatures/{idCandidat}/proposition")
    public String creer(@PathVariable int idCandidat,
                        @RequestParam BigDecimal salaire,
                        @RequestParam String dateDebut,
                        @RequestParam String lieu,
                        @RequestParam String horaires,
                        HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            propositionService.creerProposition(idCandidat, salaire,
                    LocalDateTime.parse(dateDebut), lieu, horaires);
            ra.addFlashAttribute("succes", "Proposition envoyée au candidat.");
            return "redirect:/rh/propositions";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/candidatures/" + idCandidat + "/proposition";
        }
    }

    @GetMapping("/rh/propositions/{id}")
    public String detail(@PathVariable int id, Model model,
                         HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);
            PropositionEmbauche p = propositionService.getById(id);
            if (p == null) throw new RuntimeException("Proposition introuvable");
            model.addAttribute("proposition", p);
            model.addAttribute("questions", propositionService.getQuestions(id));
            model.addAttribute("historique", propositionService.getHistorique(id));
            model.addAttribute("titre", "Proposition");
            return "demandes/proposition-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/propositions";
        }
    }

    @PostMapping("/rh/propositions/{id}/valider")
    public String valider(@PathVariable int id,
                          @RequestParam boolean accepte,
                          @RequestParam(required = false) String reponseRh,
                          HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            propositionService.validerParRh(id, accepte, reponseRh, u.getId());
            ra.addFlashAttribute("succes", accepte ? "Proposition validée." : "Proposition refusée.");
            return "redirect:/rh/propositions/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/propositions/" + id;
        }
    }

    @GetMapping("/rh/propositions/{id}/contrat.pdf")
    public ResponseEntity<byte[]> telechargerContrat(@PathVariable int id,
                                                      HttpSession session) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierRh(u);
            byte[] pdf = contratPdfService.genererPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("contrat_" + id + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void verifierRh(Utilisateur u) throws Exception {
        if (u == null || u.getRole() == null || !"RH".equalsIgnoreCase(u.getRole().getSigle())) {
            throw new Exception("Accès réservé aux RH");
        }
    }
}