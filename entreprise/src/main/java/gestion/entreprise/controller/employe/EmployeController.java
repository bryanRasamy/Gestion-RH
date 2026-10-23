package gestion.entreprise.controller.employe;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class EmployeController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private EmployeService employeService;
    @Autowired private ContratWorkflowService contratWorkflow;
    @Autowired private ContratRepository contratRepo;
    @Autowired private ContratTypeRepository contratTypeRepo;
    @Autowired private ContratPdfService contratPdfService;

    /* ---------- Accueil employé ---------- */
    @GetMapping("/employe/accueil")
    public String accueil(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierEmploye(u);

            PeriodeEmploi periode = employeService.getPeriodeActive(u);
            Affectation affectation = employeService.getAffectationCourante(periode);
            Contrat contratCourant = employeService.getContratCourant(periode);

            model.addAttribute("employeur", u.getEmployer());
            model.addAttribute("periode", periode);
            model.addAttribute("affectation", affectation);
            model.addAttribute("contratCourant", contratCourant);
            model.addAttribute("titre", "Mon espace");
            model.addAttribute("menu", "accueil");
            return "employe/accueil";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    /* ---------- Onglet Contrat ---------- */
    @GetMapping("/employe/contrats")
    public String contrats(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierEmploye(u);

            PeriodeEmploi periode = employeService.getPeriodeActive(u);
            Contrat contratActuel = employeService.getContratActuel(periode);
            List<Contrat> historique = contratRepo.findAllByEmployerOrderByDateDebutDesc(u.getEmployer().getId());

            model.addAttribute("historique", historique);
            model.addAttribute("contratActuel", contratActuel);
            model.addAttribute("contrats", historique); // compatibilité ancienne vue
            model.addAttribute("titre", "Mes contrats");
            model.addAttribute("menu", "contrats");
            return "employe/contrats";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    /* ---------- Formulaire de renouvellement ---------- */
    @GetMapping("/employe/contrats/renouveler")
    public String formulaireRenouvellement(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierEmploye(u);

            PeriodeEmploi periode = employeService.getPeriodeActive(u);
            Contrat courant = employeService.getContratCourant(periode);
            if (courant == null) throw new RuntimeException("Aucun contrat courant.");

            model.addAttribute("contratCourant", courant);
            model.addAttribute("contratTypes", contratTypeRepo.findAll());
            model.addAttribute("titre", "Renouveler mon contrat");
            model.addAttribute("menu", "contrats");
            return "employe/contrat-renouveler";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employe/contrats";
        }
    }

    @PostMapping("/employe/contrats/renouveler")
    public String renouveler(@RequestParam String typeContrat,
                             @RequestParam String dateDebut,
                             @RequestParam(required = false) String dateFin,
                             @RequestParam BigDecimal salaire,
                             HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierEmploye(u);

            PeriodeEmploi periode = employeService.getPeriodeActive(u);
            Contrat courant = employeService.getContratCourant(periode);
            if (courant == null) throw new RuntimeException("Aucun contrat courant.");

            LocalDateTime dtDebut = LocalDateTime.parse(dateDebut + "T00:00:00");
            LocalDateTime dtFin = (dateFin != null && !dateFin.isEmpty())
                    ? LocalDateTime.parse(dateFin + "T23:59:59")
                    : null;

            Contrat nouveau = contratWorkflow.demanderRenouvellement(
                    courant.getId(), dtDebut, dtFin, salaire, typeContrat, u.getId());

            ra.addFlashAttribute("succes",
                    "Nouveau contrat créé. Signez-le pour l'envoyer à la DG.");
            return "redirect:/employe/contrats/" + nouveau.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employe/contrats/renouveler";
        }
    }

    /* ---------- Détail d'un contrat ---------- */
    @GetMapping("/employe/contrats/{id}")
    public String detailContrat(@PathVariable int id, Model model,
                                 HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierEmploye(u);

            Contrat c = contratRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contrat introuvable."));

            model.addAttribute("contrat", c);
            model.addAttribute("historique", contratWorkflow.getHistorique(id));
            model.addAttribute("titre", "Contrat #" + id);
            model.addAttribute("menu", "contrats");
            return "employe/contrat-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/employe/contrats";
        }
    }

    /* ---------- Signature ---------- */
    @PostMapping("/employe/contrats/{id}/signer")
    public String signer(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierEmploye(u);
            contratWorkflow.signerContrat(id);
            ra.addFlashAttribute("succes", "Contrat signé. En attente de validation DG.");
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/employe/contrats/" + id;
    }

    /* ---------- Téléchargement PDF ---------- */
    @GetMapping("/employe/contrats/{id}/pdf")
    public ResponseEntity<byte[]> telechargerPdf(@PathVariable int id, HttpSession session) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierEmploye(u);

            Contrat c = contratRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contrat introuvable."));

            if (c.getProposition() == null || c.getProposition().getCandidat() == null
                    || c.getProposition().getCandidat().getUtilisateur() == null
                    || c.getProposition().getCandidat().getUtilisateur().getId() != u.getId()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            byte[] bytes = contratPdfService.genererPdf(c.getProposition().getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("contrat_employe_" + id + ".pdf").build());
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /* ---------- Guards ---------- */
    private void verifierEmploye(Utilisateur u) throws Exception {
        if (u == null) throw new Exception("Non authentifié.");
        if (!employeService.estEmploye(u)) {
            throw new Exception("Vous n'êtes pas encore employé.");
        }
    }
}