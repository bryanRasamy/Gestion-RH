package gestion.entreprise.controller;

import gestion.entreprise.modele.Role;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.service.StatutDemande;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class RoleAccueilController {

    private final RoleRepository roleRepository;
    private final StatutDemande statutService;

    public RoleAccueilController(RoleRepository roleRepository, StatutDemande statutService) {
        this.roleRepository = roleRepository;
        this.statutService = statutService;
    }

    @GetMapping("/rh/accueil")
    public String accueilRh(@RequestParam(required = false) LocalDate dateMin,
                           @RequestParam(required = false) LocalDate dateMax,
                           @RequestParam(required = false) String statut,
                           Model model) {
        return afficherParRole("RH", dateMin, dateMax, statut, model);
    }

    @GetMapping("/finance/accueil")
    public String accueilFinance(@RequestParam(required = false) LocalDate dateMin,
                                @RequestParam(required = false) LocalDate dateMax,
                                @RequestParam(required = false) String statut,
                                Model model) {
        return afficherParRole("Finance", dateMin, dateMax, statut, model);
    }

    @GetMapping("/dg/accueil")
    public String accueilDg(@RequestParam(required = false) LocalDate dateMin,
                            @RequestParam(required = false) LocalDate dateMax,
                            @RequestParam(required = false) String statut,
                            Model model) {
        return afficherParRole("DG", dateMin, dateMax, statut, model);
    }

    @GetMapping("/manager/accueil")
    public String accueilManager(@RequestParam(required = false) LocalDate dateMin,
                                @RequestParam(required = false) LocalDate dateMax,
                                @RequestParam(required = false) String statut,
                                Model model) {
        return afficherParRole("Manager", dateMin, dateMax, statut, model);
    }

    private String afficherParRole(String libelleRole, LocalDate dateMin, LocalDate dateMax,
                                  String statut, Model model) {
        Role role = roleRepository.findByLibelleIgnoreCase(libelleRole);
        if (role == null) {
            return "redirect:/login";
        }

        model.addAttribute("idRole", role.getId());
        model.addAttribute("roleSlug", libelleRole.toLowerCase());
        model.addAttribute("demandes", statutService.lister(role.getId(), dateMin, dateMax, statut));
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("statut", statut);
        return "workflow/liste";
    }
}
