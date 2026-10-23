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

@Controller
public class DgContratController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private ContratWorkflowService workflow;

    @GetMapping("/dg/contrats")
    public String liste(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierDg(u);
            model.addAttribute("contrats", workflow.getContratsAValiderParDg());
            model.addAttribute("titre", "Contrats à valider");
            return "demandes/dg-contrats";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/dg/contrats/{id}")
    public String detail(@PathVariable int id, Model model,
                         HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierDg(u);

            model.addAttribute("contratId", id);
            model.addAttribute("historique", workflow.getHistorique(id));
            model.addAttribute("titre", "Contrat");
            return "demandes/dg-contrat-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/dg/contrats";
        }
    }

    @PostMapping("/dg/contrats/{id}/valider")
    public String valider(@PathVariable int id, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, null);
            verifierDg(u);
            workflow.validerParDg(id, u.getId());
            ra.addFlashAttribute("succes", "Contrat validé. L'employé est officiellement embauché.");
            return "redirect:/dg/contrats";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/dg/contrats/" + id;
        }
    }

    private void verifierDg(Utilisateur u) throws Exception {
        if (u == null || u.getRole() == null || !"DG".equalsIgnoreCase(u.getRole().getSigle())) {
            throw new Exception("Accès réservé à la Direction Générale.");
        }
    }
}