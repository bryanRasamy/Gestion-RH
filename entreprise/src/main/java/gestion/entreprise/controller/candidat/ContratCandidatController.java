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

@Controller
public class ContratCandidatController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private PropositionEmbaucheService propositionService;
    @Autowired private ContratRepository contratRepo;
    @Autowired private ContratWorkflowService workflow;
    @Autowired private ContratPdfService pdfService;

    @GetMapping("/mes-candidatures/{idCandidat}/contrat")
    public String afficher(@PathVariable int idCandidat, Model model,
                            HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null || !"ACCEPTEE".equals(p.getStatut().getLibelle())) {
                throw new RuntimeException("Aucun contrat disponible.");
            }

            Contrat contrat = contratRepo.findFirstByPropositionId(p.getId());
            if (contrat == null) throw new RuntimeException("Contrat introuvable.");

            model.addAttribute("utilisateur", u);
            model.addAttribute("contrat", contrat);
            model.addAttribute("historique", workflow.getHistorique(contrat.getId()));
            model.addAttribute("titre", "Mon contrat");
            model.addAttribute("menu", "mes-candidatures");
            model.addAttribute("page", "candidat/contrat");
            return "candidat/header";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures";
        }
    }

    @PostMapping("/mes-candidatures/{idCandidat}/contrat/signer")
    public String signer(@PathVariable int idCandidat, HttpSession session, RedirectAttributes ra) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            Contrat contrat = contratRepo.findFirstByPropositionId(p.getId());
            if (contrat == null) throw new RuntimeException("Contrat introuvable.");

            workflow.signerParCandidat(contrat.getId());
            ra.addFlashAttribute("succes",
                    "Contrat signé. En attente de validation par la Direction Générale.");
            return "redirect:/mes-candidatures/" + idCandidat + "/contrat";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/mes-candidatures/" + idCandidat + "/contrat";
        }
    }

    @GetMapping("/mes-candidatures/{idCandidat}/contrat/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable int idCandidat, HttpSession session) {
        try {
            verifierAcces(idCandidat, session);
            PropositionEmbauche p = propositionService.getProposition(idCandidat);
            if (p == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            byte[] bytes = pdfService.genererPdf(p.getId());
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_PDF);
            h.setContentDisposition(ContentDisposition.attachment()
                    .filename("contrat.pdf").build());
            return new ResponseEntity<>(bytes, h, HttpStatus.OK);
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