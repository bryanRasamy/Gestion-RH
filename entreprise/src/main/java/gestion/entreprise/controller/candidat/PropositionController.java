package gestion.entreprise.controller.candidat;

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

import java.util.List;

@Controller
public class PropositionController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private PropositionEmbaucheService propositionService;
    @Autowired private ContratPdfService contratPdfService;

    @GetMapping("/mes-candidatures/{idCandidat}/proposition")
    public String afficher(@PathVariable int idCandidat, Model model,
                            HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition pour ce candidat");

            model.addAttribute("utilisateur", u);
            model.addAttribute("titre", "Proposition d'embauche");
            model.addAttribute("menu", "mes-candidatures");
            model.addAttribute("proposition", p);

            if ("EN_ATTENTE_CANDIDAT".equals(p.getStatut().getLibelle())) {
                model.addAttribute("questions", propositionService.getQuestions(p.getId()));
                model.addAttribute("page", "candidat/proposition");
            } else {
                model.addAttribute("historique", propositionService.getHistorique(p.getId()));
                model.addAttribute("page", "candidat/proposition-statut");
            }
            return "candidat/header";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/proposition")
    public String repondre(@PathVariable int idCandidat,
                           @RequestParam String action,
                           @RequestParam List<Integer> questionIds,
                           @RequestParam List<String> reponses,
                           @RequestParam(required = false) List<String> remarques,
                           @RequestParam(required = false, defaultValue = "CE") String contratChoisi,
                           @RequestParam(required = false) String motifAction,
                           HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition");

            PropositionEmbauche updated = propositionService.repondre(
                    p.getId(), action, questionIds, reponses, remarques, contratChoisi, motifAction);

            String statut = updated.getStatut().getLibelle();
            if ("ACCEPTEE".equals(statut)) {
                ra.addFlashAttribute("succes", "Félicitations ! Proposition acceptée.");
            } else if ("EN_VALIDATION_RH".equals(statut)) {
                ra.addFlashAttribute("succes", "Votre choix de contrat a été transmis à la RH.");
            } else if ("EN_NEGOCIATION".equals(statut)) {
                ra.addFlashAttribute("succes", "Votre demande de négociation a été transmise à la RH.");
            } else if ("REFUSEE".equals(statut)) {
                ra.addFlashAttribute("info", "Vous avez refusé la proposition.");
            }
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/proposition/confirmer")
    public String confirmerFinal(@PathVariable int idCandidat,
                                 @RequestParam String action,
                                 HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition");

            propositionService.confirmerFinal(p.getId(), action);
            ra.addFlashAttribute("succes",
                    "ACCEPTER".equalsIgnoreCase(action) ? "Proposition acceptée !" : "Vous avez abandonné.");
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        }
    }

    @GetMapping("/mes-candidatures/{idCandidat}/proposition/contrat.pdf")
    public ResponseEntity<byte[]> telechargerContrat(@PathVariable int idCandidat, HttpSession session) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null || !"ACCEPTEE".equals(p.getStatut().getLibelle())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            byte[] pdf = contratPdfService.genererPdf(p.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("contrat_" + p.getId() + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Utilisateur verifierAcces(int idCandidat, HttpSession session) throws Exception {
        Object idU = session.getAttribute("idUtilisateur");
        if (idU == null) throw new RuntimeException("Veuillez vous connecter");
        Candidat c = candidatRepository.findById(idCandidat).orElseThrow();
        if (c.getUtilisateur() == null || c.getUtilisateur().getId() != (int) idU) {
            throw new RuntimeException("Accès non autorisé");
        }
        return utilisateurService.getById((int) idU);
    }
}