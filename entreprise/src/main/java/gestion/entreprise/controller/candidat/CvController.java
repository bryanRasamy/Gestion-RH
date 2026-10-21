package gestion.entreprise.controller.candidat;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CvController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private OffreService offreService;

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private PosteService posteService;

    // Formulaire de CV : on y arrive en cliquant sur "Postuler" d'une offre
    @GetMapping("/cv")
    public String afficherCv(@RequestParam(value = "idDetail", defaultValue = "0") int idDetail,
                             Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = null;

        try {
            Object idUtilisateur = session.getAttribute("idUtilisateur");

            if (idUtilisateur == null) {
                throw new RuntimeException("Veuillez vous connecter pour postuler");
            }

            utilisateur = utilisateurService.getCandidatById((int) idUtilisateur);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }

        try {
            if (idDetail == 0) {
                redirectAttributes.addFlashAttribute("info", "Choisissez d'abord l'offre à laquelle vous voulez postuler.");
                return "redirect:/offres";
            }

            DetailDemande offre = offreService.getOffreActive(idDetail);

            if (candidatService.aDejaPostule(utilisateur.getId(), idDetail)) {
                throw new RuntimeException("Vous avez déjà postulé à cette offre");
            }

            // Après une erreur, le formulaire saisi revient dans "candidat" (flash)
            if (!model.containsAttribute("candidat")) {
                model.addAttribute("candidat", candidatService.preparerFormulaire(utilisateur, offre));
            }

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("offre", offre);
            model.addAttribute("postes", posteService.getAllPoste());
            model.addAttribute("titre", "Mon CV en ligne");
            model.addAttribute("menu", "cv");
            model.addAttribute("page", "candidat/cv");

            return "candidat/header";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/offres";
        }
    }

    @PostMapping("/cv")
    public String postuler(@ModelAttribute Candidat candidat,
                           @RequestParam("idDetail") int idDetail,
                           @RequestParam("fichierCv") MultipartFile fichierCv,
                           @RequestParam(value = "fichierLm", required = false) MultipartFile fichierLm,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Object idUtilisateur = session.getAttribute("idUtilisateur");

            if (idUtilisateur == null) {
                throw new RuntimeException("Veuillez vous connecter pour postuler");
            }

            Utilisateur utilisateur = utilisateurService.getCandidatById((int) idUtilisateur);

            candidatService.postuler(candidat, utilisateur.getId(), idDetail, fichierCv, fichierLm);

            redirectAttributes.addFlashAttribute("succes", "Votre candidature a bien été enregistrée.");
            return "redirect:/offres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            redirectAttributes.addFlashAttribute("candidat", candidat);
            return "redirect:/cv?idDetail=" + idDetail;
        }
    }
}
