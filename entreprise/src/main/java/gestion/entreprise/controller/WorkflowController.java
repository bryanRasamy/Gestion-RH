package gestion.entreprise.controller;

import gestion.entreprise.service.CorrectionDemandeService;
import gestion.entreprise.service.StatutDemande;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping({"/validation", "/workflow"})
public class WorkflowController {
    private final StatutDemande statutService;
    private final CorrectionDemandeService correctionService;

    public WorkflowController(StatutDemande statutService, CorrectionDemandeService correctionService) {
        this.statutService = statutService;
        this.correctionService = correctionService;
    }

    @GetMapping("/{idRole}")
    public String liste(@PathVariable int idRole,
                        @RequestParam(required = false) LocalDate dateMin,
                        @RequestParam(required = false) LocalDate dateMax,
                        @RequestParam(required = false) String statut,
                        Model model) {
        model.addAttribute("idRole", idRole);
        model.addAttribute("demandes", statutService.lister(idRole, dateMin, dateMax, statut));
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("statut", statut);
        return "workflow/liste";
    }

    @GetMapping("/manager/corrections")
    public String corrections(@RequestParam(defaultValue = "0") int idManager, Model model) {
        model.addAttribute("corrections", correctionService.lister(idManager));
        return "workflow/corrections";
    }

    @PostMapping("/manager/corrections/{id}/decision")
    public String traiterCorrection(@PathVariable int id, @RequestParam boolean accepter,
                                    RedirectAttributes redirectAttributes) {
        try {
            correctionService.appliquerCorrection(id, accepter);
            redirectAttributes.addFlashAttribute("message", "Correction traitee.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return "redirect:/validation/manager/corrections";
    }
}
