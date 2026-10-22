package gestion.entreprise.controller.workflow;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.security.AutorisationService;
import gestion.entreprise.security.EtapeValidation;
import gestion.entreprise.security.Role;
import gestion.entreprise.service.BudgetService;
import gestion.entreprise.service.WorkflowDemandeService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ValidationController {

    private final AutorisationService autorisationService;
    private final WorkflowDemandeService workflowDemandeService;
    private final BudgetService budgetService;

    public ValidationController(AutorisationService autorisationService,
            WorkflowDemandeService workflowDemandeService,
            BudgetService budgetService) {
        this.autorisationService = autorisationService;
        this.workflowDemandeService = workflowDemandeService;
        this.budgetService = budgetService;
    }

    @GetMapping("/{module:rh|finance|dg}/demandes")
    public String afficherDemandes(@PathVariable String module, Model model, HttpSession session) {
        try {
            ModuleValidation contexte = ModuleValidation.depuis(module);
            Utilisateur utilisateur = autorisationService.exiger(session, contexte.role());

            model.addAttribute("utilisateur", utilisateur);
            model.addAttribute("demandes", workflowDemandeService.aTraiter(contexte.etape()));
            model.addAttribute("titre", contexte.titre());
            model.addAttribute("etape", contexte.etape().name());

            if (contexte.etape() == EtapeValidation.FINANCE) {
                model.addAttribute("budgetService", budgetService);
            }
            return "workflow/demandes";
        } catch (Exception exception) {
            return "redirect:/login";
        }
    }

    @PostMapping("/{module:rh|finance|dg}/demandes/{id}/decision")
    public String enregistrerDecision(@PathVariable String module, @PathVariable int id,
            @RequestParam boolean accepter, @RequestParam(required = false) String commentaire,
            HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            ModuleValidation contexte = ModuleValidation.depuis(module);
            Utilisateur utilisateur = autorisationService.exiger(session, contexte.role());

            // Finance doit calculer le budget, mais la personne décide elle-même.
            if (contexte.etape() == EtapeValidation.FINANCE) {
                budgetService.evaluer(id);
            }

            workflowDemandeService.decider(id, contexte.etape(), accepter,
                    utilisateur.getEmployer(), commentaire);
            redirectAttributes.addFlashAttribute("succes", "Décision enregistrée.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return "redirect:/" + module + "/demandes";
    }

    private record ModuleValidation(Role role, EtapeValidation etape, String titre) {
        static ModuleValidation depuis(String module) {
            return switch (module.toLowerCase()) {
                case "rh" -> new ModuleValidation(Role.RH, EtapeValidation.RH, "Ressources humaines");
                case "finance" -> new ModuleValidation(Role.FINANCE, EtapeValidation.FINANCE, "Finance");
                case "dg" -> new ModuleValidation(Role.DG, EtapeValidation.DG, "Direction générale");
                default -> throw new IllegalArgumentException("Module de validation inconnu");
            };
        }
    }
}
