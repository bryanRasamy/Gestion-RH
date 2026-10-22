package gestion.entreprise.controller;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.service.BudgetService;
import gestion.entreprise.service.DemandeARevoirService;
import gestion.entreprise.service.DemandeService;
import gestion.entreprise.service.DemandeStatutService;
import gestion.entreprise.service.RoleService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ValidationController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private DemandeStatutService statutService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private DemandeARevoirService revisionService;

    @Autowired
    private BudgetService budgetService;

    private void verifierRoleValidation(int idUtilisateur, String role) {
        if (!"RH".equals(role) && !"FINANCE".equals(role) && !"DG".equals(role)) {
            throw new RuntimeException("Espace de validation inconnu");
        }
        roleService.verifierRole(idUtilisateur, role);
    }

    @GetMapping("/{role}/accueil")
    public String accueil(@PathVariable String role,
                          @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                          Model model, RedirectAttributes redirectAttributes) {
        try {
            int id = roleService.convertirId(idUtilisateur);
            String roleAttendu = role.toUpperCase();
            verifierRoleValidation(id, roleAttendu);

            String statut = "RH".equals(roleAttendu) ? "créé"
                : "FINANCE".equals(roleAttendu) ? "validé RH" : "validé Finance";
            List<Demande> demandes = statutService.listerParStatut(statut);
            List<List<DetailDemande>> details = new ArrayList<>();
            List<double[]> montants = new ArrayList<>();
            List<String> evaluations = new ArrayList<>();
            double budgetGlobal = 0;
            boolean budgetConfigure = true;
            if ("FINANCE".equals(roleAttendu)) {
                try {
                    budgetGlobal = budgetService.budgetDisponible();
                } catch (RuntimeException erreur) {
                    budgetConfigure = false;
                }
            }
            for (Demande demande : demandes) {
                details.add(demandeService.details(demande.getId()));
                if ("FINANCE".equals(roleAttendu)) {
                    try {
                        montants.add(budgetService.montantDemande(demande.getId()));
                        evaluations.add(budgetService.evaluer(demande.getId()));
                    } catch (RuntimeException erreur) {
                        montants.add(new double[] {0, 0});
                        evaluations.add("Grille non configurée");
                    }
                }
            }
            model.addAttribute("demandes", demandes);
            model.addAttribute("details", details);
            model.addAttribute("montants", montants);
            model.addAttribute("evaluations", evaluations);
            model.addAttribute("budgetGlobal", budgetGlobal);
            model.addAttribute("budgetConfigure", budgetConfigure);
            model.addAttribute("role", roleAttendu);
            model.addAttribute("superAdmin", roleService.estSuperAdmin(id));
            model.addAttribute("statut", statut);
            return "demande/validation";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/{role}/demande/{id}")
    public String detailValidation(@PathVariable String role, @PathVariable int id,
                                   @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                   Model model, RedirectAttributes redirectAttributes) {
        String roleAttendu = role.toUpperCase();
        try {
            int idUtilisateurConnecte = roleService.convertirId(idUtilisateur);
            verifierRoleValidation(idUtilisateurConnecte, roleAttendu);

            String statutAttendu = "RH".equals(roleAttendu) ? "créé"
                : "FINANCE".equals(roleAttendu) ? "validé RH" : "validé Finance";
            Demande demande = demandeService.demande(id);
            if (!statutAttendu.equalsIgnoreCase(demandeService.statut(id))) {
                throw new RuntimeException("Cette demande n'est pas dans votre file de validation");
            }

            model.addAttribute("demande", demande);
            model.addAttribute("details", demandeService.details(id));
            model.addAttribute("statut", statutAttendu);
            model.addAttribute("role", roleAttendu);
            model.addAttribute("superAdmin", roleService.estSuperAdmin(idUtilisateurConnecte));
            model.addAttribute("retour", "/" + role.toLowerCase() + "/accueil");
            if ("FINANCE".equals(roleAttendu)) {
                try {
                    model.addAttribute("budgetGlobal", budgetService.budgetDisponible());
                    model.addAttribute("budgetConfigure", true);
                } catch (RuntimeException erreur) {
                    model.addAttribute("budgetConfigure", false);
                }
                try {
                    double[] montant = budgetService.montantDemande(id);
                    model.addAttribute("montantMinimum", montant[0]);
                    model.addAttribute("montantMaximum", montant[1]);
                    model.addAttribute("evaluation", budgetService.evaluer(id));
                } catch (RuntimeException erreur) {
                    model.addAttribute("evaluation", "Grille non configurée");
                }
            }
            return "demande/detail";
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
            return "redirect:/" + role.toLowerCase() + "/accueil";
        }
    }

    @PostMapping("/{role}/demande/{id}/valider")
    public String valider(@PathVariable String role, @PathVariable int id,
                          @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                          RedirectAttributes redirectAttributes) {
        String roleAttendu = role.toUpperCase();
        try {
            int idUser = roleService.convertirId(idUtilisateur);
            verifierRoleValidation(idUser, roleAttendu);
            if ("FINANCE".equals(roleAttendu) && !"OK".equals(budgetService.evaluer(id))) {
                throw new RuntimeException("Le budget ne permet pas de valider cette demande; demandez une révision ou refusez-la avec un motif");
            }
            String statut = "RH".equals(roleAttendu) ? "validé RH"
                : "FINANCE".equals(roleAttendu) ? "validé Finance" : "validé DG";
            statutService.changer(id, statut, roleService.employerConnecte(idUser), null);
            redirectAttributes.addFlashAttribute("succes", "La demande a été validée");
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
        }
        return "redirect:/" + role.toLowerCase() + "/accueil";
    }

    @PostMapping("/{role}/demande/{id}/refuser")
    public String refuser(@PathVariable String role, @PathVariable int id,
                          @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                          @RequestParam(name = "motif", required = false) String motif,
                          RedirectAttributes redirectAttributes) {
        try {
            int idUser = roleService.convertirId(idUtilisateur);
            verifierRoleValidation(idUser, role.toUpperCase());
            statutService.changer(id, "refusé", roleService.employerConnecte(idUser), motif);
            redirectAttributes.addFlashAttribute("succes", "La demande a été refusée");
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
        }
        return "redirect:/" + role.toLowerCase() + "/accueil";
    }

    @PostMapping("/{role}/demande/{id}/revision")
    public String demanderRevision(@PathVariable String role, @PathVariable int id,
                                   @SessionAttribute(name = "idUtilisateur", required = false) String idUtilisateur,
                                   @RequestParam(name = "detailId") int detailId,
                                   @RequestParam(name = "commentaire", required = false) String commentaire,
                                   @RequestParam(name = "nombrePropose", defaultValue = "0") int nombrePropose,
                                   @RequestParam(name = "annulerPoste", defaultValue = "false") boolean annulerPoste,
                                   RedirectAttributes redirectAttributes) {
        try {
            int idUser = roleService.convertirId(idUtilisateur);
            String roleAttendu = role.toUpperCase();
            verifierRoleValidation(idUser, roleAttendu);
            String etape = "FINANCE".equals(roleAttendu) ? "Finance" : roleAttendu;
            revisionService.demanderRevision(id, roleService.employerConnecte(idUser), etape,
                detailId, commentaire, nombrePropose, annulerPoste);
            redirectAttributes.addFlashAttribute("succes", "La demande a été envoyée en révision");
        } catch (RuntimeException erreur) {
            redirectAttributes.addFlashAttribute("erreur", erreur.getMessage());
        }
        return "redirect:/" + role.toLowerCase() + "/accueil";
    }
}
