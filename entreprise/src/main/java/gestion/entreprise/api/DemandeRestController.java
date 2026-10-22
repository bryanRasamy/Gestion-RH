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

    @Autowired
    private DiplomeRepository diplomeRepository;

    /**
     * POST /api/demandes/{id}/accepter
     * Tout rôle authentifié. Change le statut à AC. id_employer = celui qui fait l'action.
     */
    @PostMapping("/{id}/accepter")
    public ResponseEntity<Map<String, Object>> accepter(@PathVariable int id,
            @RequestBody(required = false) Map<String, String> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);

            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            Demande demande = demandeService.getById(id);

            Status statusAccepter = statusRepository.findBySigle("AC");
            if (statusAccepter == null) {
                throw new RuntimeException("Statut 'Accepter' introuvable.");
            }

            String motif = (body != null) ? body.get("motif") : null;

            historiqueDemandeStatusService.changerStatut(demande, statusAccepter, utilisateur.getEmployer(), motif);

            response.put("succes", true);
            response.put("message", "Demande acceptée.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/demandes/{id}/valider
     * Tout rôle authentifié. Change le statut à VA.
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
     * POST /api/demandes/{id}/a-revoir
     * Tout rôle. Motif obligatoire. Corrections optionnelles (snapshot detail_demande).
     * Body: { "motif": "...", "corrections": [ { "idDetail": X, "nbSouhaite": N, "idDiplome": D, "nbExperience": E } ] }
     */
    @PostMapping("/{id}/a-revoir")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> aRevoir(@PathVariable int id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);

            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            String motif = (String) body.get("motif");
            if (motif == null || motif.trim().isEmpty()) {
                throw new RuntimeException("Le motif est obligatoire pour mettre une demande à revoir.");
            }

            Demande demande = demandeService.getById(id);

            Status statusARevoir = statusRepository.findBySigle("AR");
            if (statusARevoir == null) {
                throw new RuntimeException("Statut 'A Revoir' introuvable.");
            }

            historiqueDemandeStatusService.changerStatut(demande, statusARevoir, utilisateur.getEmployer(), motif.trim());

            // Snapshot des corrections si fournies
            List<Map<String, Object>> corrections = (List<Map<String, Object>>) body.get("corrections");
            if (corrections != null) {
                for (Map<String, Object> corr : corrections) {
                    int idDetail = ((Number) corr.get("idDetail")).intValue();
                    int nbSouhaite = ((Number) corr.get("nbSouhaite")).intValue();
                    int idDiplome = ((Number) corr.get("idDiplome")).intValue();
                    int nbExperience = ((Number) corr.get("nbExperience")).intValue();

                    if (nbSouhaite <= 0) {
                        throw new RuntimeException("Le nombre souhaité doit être strictement positif.");
                    }
                    if (nbExperience < 0) {
                        throw new RuntimeException("Le nombre d'expérience ne peut pas être négatif.");
                    }

                    DetailDemande detail = detailDemandeRepository.findById(idDetail).orElse(null);
                    if (detail == null) {
                        throw new RuntimeException("Detail de demande introuvable: " + idDetail);
                    }

                    Diplome diplome = diplomeRepository.findById(idDiplome).orElse(null);
                    if (diplome == null) {
                        throw new RuntimeException("Diplôme introuvable: " + idDiplome);
                    }

                    DemandeARevoir demandeARevoir = new DemandeARevoir();
                    demandeARevoir.setDemandeur(utilisateur.getEmployer());
                    demandeARevoir.setDemandeDetails(detail);
                    demandeARevoir.setNbSouhaite(nbSouhaite);
                    demandeARevoir.setDiplome(diplome);
                    demandeARevoir.setNbExperience(nbExperience);

                    demandeARevoirRepository.save(demandeARevoir);
                }
            }

            response.put("succes", true);
            response.put("message", "Demande marquée à revoir.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("succes", false);
            response.put("erreur", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * POST /api/demandes/{id}/accepter-correction
     * Tout rôle. Le statut passe à AC avec comme employer le correcteur (qui a demandé la review).
     */
    @PostMapping("/{id}/accepter-correction")
    public ResponseEntity<Map<String, Object>> accepterCorrection(@PathVariable int id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, null);

            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            Demande demande = demandeService.getById(id);

            // Trouver le correcteur via les DemandeARevoir
            List<DetailDemande> details = detailDemandeRepository.findByDemandeId(id);
            Employer correcteur = null;
            for (DetailDemande detail : details) {
                List<DemandeARevoir> revisions = demandeARevoirRepository.findByDemandeDetailsId(detail.getId());
                if (!revisions.isEmpty()) {
                    correcteur = revisions.get(0).getDemandeur();
                    break;
                }
            }

            if (correcteur == null) {
                correcteur = utilisateur.getEmployer();
            }

            Status statusAccepter = statusRepository.findBySigle("AC");
            if (statusAccepter == null) {
                throw new RuntimeException("Statut 'Accepter' introuvable.");
            }

            historiqueDemandeStatusService.changerStatut(demande, statusAccepter, correcteur, null);

            response.put("succes", true);
            response.put("message", "Corrections acceptées.");
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
