package gestion.entreprise.api;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandes")
public class DemandeRestController {

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private HistoriqueDemandeStatusService historiqueDemandeStatusService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private StatusRepository statusRepository;

    @PostMapping("/{id}/valider")
    public ResponseEntity<Map<String, Object>> valider(@PathVariable int id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);
            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            demandeService.verifierNonCloturee(id);

            Demande demande = demandeService.getById(id);

            Status statusValider = statusRepository.findBySigle("VA");
            if (statusValider == null) {
                throw new RuntimeException("Statut 'Valider' introuvable.");
            }

            String motif = (body != null) ? body.get("motif") : null;
            historiqueDemandeStatusService.changerStatut(demande, statusValider, utilisateur.getEmployer(), motif);

            response.put("succes", true);
            response.put("message", "Demande validée.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/demandes/{id}/valider-correction
     * Réservé au MANAGER quand la demande est en statut AR.
     * Insère les snapshots dans demande_a_revoir, puis pose le statut VA.
     */
    @PostMapping("/{id}/valider-correction")
    public ResponseEntity<Map<String, Object>> validerCorrection(@PathVariable int id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);
            if (utilisateur.getRole() == null
                    || !"MANAGER".equalsIgnoreCase(utilisateur.getRole().getSigle())) {
                throw new RuntimeException("Seul un manager peut valider une correction.");
            }
            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            Demande demande = demandeService.getById(id);

            String motif = (body != null && body.get("motif") != null)
                    ? body.get("motif")
                    : "Correction acceptée par le manager.";

            historiqueDemandeStatusService.validerCorrection(
                    demande, utilisateur.getEmployer(), motif);

            response.put("succes", true);
            response.put("message", "Correction validée.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<Map<String, Object>> refuser(@PathVariable int id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);
            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            String motif = body.get("motif");
            if (motif == null || motif.trim().isEmpty()) {
                throw new RuntimeException("Le motif est obligatoire pour refuser une demande.");
            }

            demandeService.verifierNonCloturee(id);

            Demande demande = demandeService.getById(id);

            Status statusRefuser = statusRepository.findBySigle("RF");
            if (statusRefuser == null) {
                throw new RuntimeException("Statut 'Refuser' introuvable.");
            }

            historiqueDemandeStatusService.changerStatut(demande, statusRefuser, utilisateur.getEmployer(), motif.trim());

            response.put("succes", true);
            response.put("message", "Demande refusée.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/abandonner")
    public ResponseEntity<Map<String, Object>> abandonner(@PathVariable int id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);
            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            demandeService.verifierNonCloturee(id);

            Demande demande = demandeService.getById(id);

            Status statusAnnuler = statusRepository.findBySigle("AN");
            if (statusAnnuler == null) {
                throw new RuntimeException("Statut 'Annuler' introuvable.");
            }

            historiqueDemandeStatusService.changerStatut(demande, statusAnnuler, utilisateur.getEmployer(), "Demande abandonnée.");

            response.put("succes", true);
            response.put("message", "Demande abandonnée.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}