package gestion.entreprise.controller.candidat;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OffreController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private OffreService offreService;

    @Autowired
    private CandidatService candidatService;

    // Après login / inscription, l'utilisateur normal arrive sur /accueil : on l'envoie vers les offres
    @GetMapping("/accueil")
    public String accueil() {
        return "redirect:/offres";
    }

    @GetMapping("/offres")
    public String getListeOffre(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Object idUtilisateur = session.getAttribute("idUtilisateur");

            if (idUtilisateur == null) {
                throw new RuntimeException("Veuillez vous connecter pour voir les offres");
            }

            Utilisateur utilisateur = utilisateurService.getCandidatById((int) idUtilisateur);

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("offres", offreService.getOffresActives());
            model.addAttribute("titre", "Offres d'emploi");
            model.addAttribute("menu", "offres");
            model.addAttribute("page", "candidat/offres");

            return "candidat/header";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/offres/{idDetail}")
    public String getDetailOffre(@PathVariable("idDetail") int idDetail, Model model, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Utilisateur utilisateur = null;

        try {
            Object idUtilisateur = session.getAttribute("idUtilisateur");

            if (idUtilisateur == null) {
                throw new RuntimeException("Veuillez vous connecter pour voir les offres");
            }

            utilisateur = utilisateurService.getCandidatById((int) idUtilisateur);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }

        try {
            DetailDemande offre = offreService.getOffreActive(idDetail);

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("offre", offre);
            model.addAttribute("dejaPostule", candidatService.aDejaPostule(utilisateur.getId(), idDetail));
            model.addAttribute("titre", "Détail de l'offre");
            model.addAttribute("menu", "offres");
            model.addAttribute("page", "candidat/detailOffre");

            return "candidat/header";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/offres";
        }
    }
}
