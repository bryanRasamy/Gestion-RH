package gestion.entreprise.api;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.service.BudgetService;
import gestion.entreprise.service.CorrectionDemandeService;
import gestion.entreprise.service.StatutDemande;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/validation", "/api/workflow"})
public class WorkflowRestController {
    private final StatutDemande statutService;
    private final BudgetService budgetService;
    private final CorrectionDemandeService correctionService;

    public WorkflowRestController(StatutDemande statutService, BudgetService budgetService,
                                  CorrectionDemandeService correctionService) {
        this.statutService = statutService;
        this.budgetService = budgetService;
        this.correctionService = correctionService;
    }

    @GetMapping("/{idRole}")
    public List<Map<String, Object>> lister(@PathVariable int idRole,
                                             @RequestParam(required = false) LocalDate dateMin,
                                             @RequestParam(required = false) LocalDate dateMax,
                                             @RequestParam(required = false) String statut) {
        List<Demande> demandes = statutService.lister(idRole, dateMin, dateMax, statut);
        List<Map<String, Object>> resultat = new java.util.ArrayList<Map<String, Object>>();
        for (Demande demande : demandes) {
            Map<String, Object> ligne = new HashMap<>();
            ligne.put("id", demande.getId());
            ligne.put("matricule", demande.getMatricule());
            ligne.put("dateLimite", demande.getDateLimite());
            ligne.put("montant", budgetService.montantDemande(demande.getId()));
            ligne.put("budget", budgetService.budgetCourant());
            ligne.put("validable", budgetService.estDansBudget(demande.getId()));
            resultat.add(ligne);
        }
        return resultat;
    }

    @PostMapping("/{idRole}/{idDemande}/decision")
    public ResponseEntity<Map<String, String>> traiter(@PathVariable int idRole, @PathVariable int idDemande,
                                                        @RequestBody Map<String, Object> corps) {
        try {
            int idEmploye = ((Number) corps.get("idEmploye")).intValue();
            Integer propose = corps.get("nbrPersonnelPropose") == null ? null
                    : ((Number) corps.get("nbrPersonnelPropose")).intValue();
            statutService.traiter(idDemande, idEmploye, idRole,
                    (String) corps.get("decision"), (String) corps.get("motif"), propose);
            return ResponseEntity.ok(Map.of("message", "Décision enregistrée."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
        }
    }

    @GetMapping("/manager/corrections")
    public List<?> corrections(@RequestParam(defaultValue = "0") int idManager) {
        return correctionService.lister(idManager);
    }

    @PostMapping("/manager/corrections/{id}/decision")
    public ResponseEntity<Map<String, String>> corriger(@PathVariable int id,
                                                         @RequestParam boolean accepter) {
        try {
            correctionService.appliquerCorrection(id, accepter);
            return ResponseEntity.ok(Map.of("message", "Correction traitée."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
        }
    }
}
