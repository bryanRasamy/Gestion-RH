package gestion.entreprise.api;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.service.DemandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
public class DemandeRestController {

    private final DemandeService demandeService;

    public DemandeRestController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> creer(@RequestBody Map<String, Object> attributes) {
        try {
            int idEmployer = ((Number) attributes.get("idEmployer")).intValue();
            LocalDate dateLimite = LocalDate.parse((String) attributes.get("dateLimite"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lignes = (List<Map<String, Object>>) attributes.get("lignes");

            Demande saved = demandeService.creerDemande(idEmployer, dateLimite, lignes);
            return ResponseEntity.ok(reponse(true, "Demande créée avec succès.", saved.getMatricule()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(reponse(false, "Erreur : " + e.getMessage(), null));
        }
    }

    private Map<String, Object> reponse(boolean success, String message, String matricule) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("matricule", matricule);
        return response;
    }
}