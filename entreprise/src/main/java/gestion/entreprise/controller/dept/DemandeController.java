package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;
import gestion.entreprise.vue.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departements")
public class DemandeController {

    @Autowired 
    private HistoriqueDemandeStatusService historiqueDemandeStatusService;

    @Autowired 
    private UtilisateurService utilisateurService;

    @GetMapping("/accueil")
    public String getListeDemande(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.getById((int) session.getAttribute("idUtilisateur"));

            Employer employer=utilisateur.getEmployer();

            if (employer==null) {
                throw new RuntimeException("Utilisateur non autoriser. Seule les employer peuvent acceder à cette page");
            }

            Poste poste=employer.getPoste();

            if (!poste.getSigle().equalsIgnoreCase("MI") && !poste.getSigle().equalsIgnoreCase("MC") && !poste.getSigle().equalsIgnoreCase("DG")) {
                throw new RuntimeException("Seules les manager sont autoriser à acceder à cette page :"+poste.getSigle());
            }

            Departement departement=poste.getDepartement();

            if (departement.getSigle().equalsIgnoreCase("rh")) {
                throw new RuntimeException("Les rh ne sont pas autoriser à acceder à cette page");
            }

            List<HistoriqueDemandeStatusView> demandes = historiqueDemandeStatusService.getDemandesRecent();

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("poste", poste);
            model.addAttribute("departement", departement);
            model.addAttribute("demandes", demandes);

            return "departements/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
        

        
    }
}
