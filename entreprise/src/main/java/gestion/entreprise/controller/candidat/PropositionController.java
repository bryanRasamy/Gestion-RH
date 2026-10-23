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
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition pour ce candidat");

            if ("EN_ATTENTE_CANDIDAT".equals(p.getStatut().getLibelle())) {
                model.addAttribute("proposition", p);
                model.addAttribute("questions", propositionService.getQuestions(p.getId()));
                model.addAttribute("titre", "Proposition d'embauche");
                return "candidat/proposition";
            } else {
                model.addAttribute("proposition", p);
                model.addAttribute("historique", propositionService.getHistorique(p.getId()));
                model.addAttribute("titre", "Proposition d'embauche");
                return "candidat/proposition-statut";
            }
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/proposition")
    public String repondre(@PathVariable int idCandidat,
                           @RequestParam List<Integer> questionIds,
                           @RequestParam List<String> reponses,
                           @RequestParam List<String> remarques,
                           @RequestParam String contratChoisi,
                           HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition");

            PropositionEmbauche updated = propositionService.repondre(p.getId(), questionIds,
                    reponses, remarques, contratChoisi);

            String statut = updated.getStatut().getLibelle();
            if ("ACCEPTEE".equals(statut)) {
                ra.addFlashAttribute("succes", "Félicitations ! Votre proposition est acceptée. "
                        + "Vous pouvez télécharger votre contrat.");
            } else if ("EN_VALIDATION_RH".equals(statut)) {
                ra.addFlashAttribute("succes", "Votre choix de contrat a été transmis à la RH.");
            } else {
                ra.addFlashAttribute("succes", "Votre demande de négociation a été transmise à la RH.");
            }
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures/" + idCandidat + "/proposition";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/proposition/abandonner")
    public String abandonner(@PathVariable int idCandidat, HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) throw new RuntimeException("Aucune proposition");
            propositionService.abandonnerCandidat(p.getId());
            ra.addFlashAttribute("succes", "Vous avez abandonné la proposition.");
            return "redirect:/mes-candidatures";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures";
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

    private void verifierAcces(int idCandidat, HttpSession session) throws Exception {
        Object idU = session.getAttribute("idUtilisateur");
        if (idU == null) throw new RuntimeException("Veuillez vous connecter");
        Candidat c = candidatRepository.findById(idCandidat).orElseThrow();
        if (c.getUtilisateur() == null || c.getUtilisateur().getId() != (int) idU) {
            throw new RuntimeException("Accès non autorisé");
        }
    }
}