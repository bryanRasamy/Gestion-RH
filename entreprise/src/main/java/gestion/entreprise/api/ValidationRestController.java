package gestion.entreprise.api;

import gestion.entreprise.config.UtilisateurActif;
import gestion.entreprise.dto.DemandeCarte;
import gestion.entreprise.modele.StatutDemande;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.service.CorrectionService;
import gestion.entreprise.service.UtilisateurService;
import gestion.entreprise.service.ValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API du circuit de validation : consultation des demandes de l'etage du
 * role connecte et actions (valider / a revoir / refuser).
 */
@RestController
@RequestMapping("/api/validation")
public class ValidationRestController {

    private final ValidationService validationService;
    private final CorrectionService correctionService;
    private final UtilisateurService utilisateurService;

    public ValidationRestController(ValidationService validationService,
                                    CorrectionService correctionService,
                                    UtilisateurService utilisateurService) {
        this.validationService = validationService;
        this.correctionService = correctionService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/demandes")
    public ResponseEntity<Map<String, Object>> lister(
            @UtilisateurActif Utilisateur utilisateur,
            @RequestParam(required = false) LocalDate dateMin,
            @RequestParam(required = false) LocalDate dateMax,
            @RequestParam(required = false) String statut) {
        try {
            TypeRole role = utilisateurService.role(utilisateur);
            if (role == null) {
                throw new IllegalStateException("Aucun role ne vous est attribue.");
            }
            List<DemandeCarte> demandes = validationService.listerCartes(
                    utilisateur.getIdRole(), dateMin, dateMax, statut);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("role", role.getLibelle());
            body.put("demandes", demandes);
            return ResponseEntity.ok(body);
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    @GetMapping("/corrections")
    public ResponseEntity<Map<String, Object>> corrections(@UtilisateurActif Utilisateur utilisateur) {
        try {
            if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
                throw new IllegalStateException("Action reservee au manager.");
            }
            List<DemandeCarte> demandes = correctionService
                    .listerDemandesARevoir(utilisateurService.idEmploye(utilisateur)).stream()
                    .map(validationService::carte)
                    .toList();
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("demandes", demandes);
            return ResponseEntity.ok(body);
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    @PostMapping("/demandes/{id}/valider")
    public ResponseEntity<Map<String, Object>> valider(@UtilisateurActif Utilisateur utilisateur,
                                                       @PathVariable int id) {
        try {
            StatutDemande cible = StatutDemande.statutValidePar(utilisateurService.role(utilisateur));
            if (cible == null) {
                throw new IllegalStateException("Votre role ne valide pas les demandes.");
            }
            validationService.changer(id, cible, utilisateurService.idEmploye(utilisateur), null);
            return ResponseEntity.ok(succes("Demande validee."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    @PostMapping("/demandes/{id}/refuser")
    public ResponseEntity<Map<String, Object>> refuser(@UtilisateurActif Utilisateur utilisateur,
                                                       @PathVariable int id,
                                                       @RequestBody(required = false) Map<String, Object> corps) {
        try {
            String motif = corps != null ? (String) corps.get("motif") : null;
            validationService.changer(id, StatutDemande.REFUSER,
                    utilisateurService.idEmploye(utilisateur), motif);
            return ResponseEntity.ok(succes("Demande refusee."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    @PostMapping("/demandes/{id}/revoir")
    public ResponseEntity<Map<String, Object>> revoir(@UtilisateurActif Utilisateur utilisateur,
                                                      @PathVariable int id,
                                                      @RequestBody(required = false) Map<String, Object> corps) {
        try {
            String commentaire = corps != null ? (String) corps.get("commentaire") : null;
            List<ValidationService.LigneRevision> lignes = new ArrayList<>();
            if (corps != null && corps.get("lignes") instanceof List<?> brut) {
                for (Object element : brut) {
                    if (!(element instanceof Map<?, ?> ligne)) {
                        continue;
                    }
                    Object idDetail = ligne.get("idDemandeDetails");
                    if (!(idDetail instanceof Number)) {
                        throw new IllegalArgumentException("idDemandeDetails est obligatoire.");
                    }
                    Object propose = ligne.get("nbrPersonnelPropose");
                    Integer effectif = propose instanceof Number ? ((Number) propose).intValue() : null;
                    lignes.add(new ValidationService.LigneRevision(
                            ((Number) idDetail).intValue(), effectif));
                }
            }
            validationService.demanderRevision(id, utilisateurService.idEmploye(utilisateur),
                    commentaire, lignes);
            return ResponseEntity.ok(succes("Demande remise a revoir."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    @PostMapping("/corrections/{id}/decision")
    public ResponseEntity<Map<String, Object>> decider(@UtilisateurActif Utilisateur utilisateur,
                                                       @PathVariable int id,
                                                       @RequestBody Map<String, Object> corps) {
        try {
            if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
                throw new IllegalStateException("Action reservee au manager.");
            }
            boolean accepter = Boolean.parseBoolean(String.valueOf(corps.get("accepter")));
            if (accepter) {
                correctionService.accepter(id, utilisateurService.idEmploye(utilisateur));
                return ResponseEntity.ok(succes("Correction acceptee."));
            }
            correctionService.abandonner(id, utilisateurService.idEmploye(utilisateur));
            return ResponseEntity.ok(succes("Demande abandonnee."));
        } catch (RuntimeException exception) {
            return ResponseEntity.badRequest().body(erreur(exception));
        }
    }

    private Map<String, Object> succes(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", message);
        return body;
    }

    private Map<String, Object> erreur(RuntimeException exception) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", exception.getMessage());
        return body;
    }
}
