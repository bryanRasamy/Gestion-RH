package gestion.entreprise.api;

import gestion.entreprise.config.UtilisateurActif;
import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.service.DemandeService;
import gestion.entreprise.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creation d'une demande de recrutement.
 *
 * <p>L'employe a l'origine de la demande est celui de la session : il n'est
 * pas fourni par le client.</p>
 */
@RestController
@RequestMapping("/api/demandes")
public class DemandeRestController {

    private final DemandeService demandeService;
    private final UtilisateurService utilisateurService;

    public DemandeRestController(DemandeService demandeService, UtilisateurService utilisateurService) {
        this.demandeService = demandeService;
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> creer(@UtilisateurActif Utilisateur utilisateur,
                                                     @RequestBody Map<String, Object> attributes) {
        try {
            if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
                throw new IllegalStateException("Seul un manager peut creer une demande.");
            }
            int idEmploye = utilisateurService.idEmploye(utilisateur);
            if (idEmploye <= 0) {
                throw new IllegalStateException("Aucun employe associe a votre compte.");
            }

            String dateLimiteBrut = (String) attributes.get("dateLimite");
            if (dateLimiteBrut == null || dateLimiteBrut.isBlank()) {
                throw new IllegalArgumentException("La date limite est obligatoire.");
            }
            LocalDate dateLimite = LocalDate.parse(dateLimiteBrut);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lignes = (List<Map<String, Object>>) attributes.get("lignes");

            Demande saved = demandeService.creerDemande(idEmploye, dateLimite, lignes);
            return ResponseEntity.ok(reponse(true, "Demande creee avec succes.", saved.getMatricule()));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(reponse(false, "Erreur : " + exception.getMessage(), null));
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
