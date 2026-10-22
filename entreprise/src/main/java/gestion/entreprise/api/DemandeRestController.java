package gestion.entreprise.api;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private DemandeARevoirRepository demandeARevoirRepository;

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    /**
     * POST /api/demandes/{id}/valider
     * Tout rôle authentifié. Change le statut à VA. id_employer = celui qui fait l'action.
     */
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
     * POST /api/demandes/{id}/refuser
     * Tout rôle authentifié. Change le statut à RF. Motif obligatoire.
     */
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

    /**
     * POST /api/demandes/{id}/abandonner
     * Tout rôle. Statut = AN.
     */
    @PostMapping("/{id}/abandonner")
    public ResponseEntity<Map<String, Object>> abandonner(@PathVariable int id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);

            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

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
