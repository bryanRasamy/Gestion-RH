package gestion.entreprise.controller;

import gestion.entreprise.config.UtilisateurActif;
import gestion.entreprise.dto.DemandeCarte;
import gestion.entreprise.dto.EcranCorrection;
import gestion.entreprise.modele.StatutDemande;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.service.CorrectionService;
import gestion.entreprise.service.UtilisateurService;
import gestion.entreprise.service.ValidationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ecran de validation : liste des demandes de l'etage du role connecte,
 * actions (valider / a revoir / refuser) et correction par le manager.
 */
@Controller
@RequestMapping("/validation")
public class ValidationController {

    private final ValidationService validationService;
    private final CorrectionService correctionService;
    private final UtilisateurService utilisateurService;

    public ValidationController(ValidationService validationService,
                                CorrectionService correctionService,
                                UtilisateurService utilisateurService) {
        this.validationService = validationService;
        this.correctionService = correctionService;
        this.utilisateurService = utilisateurService;
    }

    /* ------------------------------------------------------------------ */
    // Listes
    /* ------------------------------------------------------------------ */

    @GetMapping
    public String liste(@UtilisateurActif Utilisateur utilisateur,
                        @RequestParam(required = false) LocalDate dateMin,
                        @RequestParam(required = false) LocalDate dateMax,
                        @RequestParam(required = false) String statut,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        TypeRole role = utilisateurService.role(utilisateur);
        if (role == null) {
            return "redirect:/accueil";
        }
        List<DemandeCarte> demandes;
        try {
            demandes = validationService.listerCartes(
                    utilisateur.getIdRole(), dateMin, dateMax, statut);
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
            return "redirect:/validation";
        }

        model.addAttribute("demandes", demandes);
        model.addAttribute("role", role.getLibelle());
        model.addAttribute("roleType", role);
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("dateMin", dateMin);
        model.addAttribute("dateMax", dateMax);
        model.addAttribute("statut", statut);
        model.addAttribute("page", "validation");
        return "validation/liste";
    }

    @GetMapping("/manager/corrections")
    public String corrections(@UtilisateurActif Utilisateur utilisateur, Model model) {
        if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
            return "redirect:/validation";
        }
        List<DemandeCarte> demandes = correctionService.listerDemandesARevoir(
                utilisateurService.idEmploye(utilisateur)).stream()
                .map(validationService::carte)
                .toList();

        model.addAttribute("demandes", demandes);
        model.addAttribute("role", TypeRole.MANAGER.getLibelle());
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("page", "corrections");
        return "validation/corrections";
    }

    @GetMapping("/manager/corrections/{id}")
    public String correctionDetail(@UtilisateurActif Utilisateur utilisateur,
                                   @PathVariable int id, Model model,
                                   RedirectAttributes redirectAttributes) {
        if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
            return "redirect:/validation";
        }
        EcranCorrection ecran;
        try {
            ecran = correctionService.ecran(id, utilisateurService.idEmploye(utilisateur));
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
            return "redirect:/validation/manager/corrections";
        }

        model.addAttribute("correction", ecran);
        model.addAttribute("role", TypeRole.MANAGER.getLibelle());
        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("nomUtilisateur", utilisateurService.nomAffiche(utilisateur));
        model.addAttribute("page", "corrections");
        return "validation/correction-detail";
    }

    /* ------------------------------------------------------------------ */
    // Actions sur une demande
    /* ------------------------------------------------------------------ */

    @PostMapping("/demandes/{id}/valider")
    public String valider(@UtilisateurActif Utilisateur utilisateur, @PathVariable int id,
                          RedirectAttributes redirectAttributes) {
        try {
            StatutDemande cible = StatutDemande.statutValidePar(utilisateurService.role(utilisateur));
            if (cible == null) {
                throw new IllegalStateException("Votre role ne valide pas les demandes.");
            }
            validationService.changer(id, cible, utilisateurService.idEmploye(utilisateur), null);
            redirectAttributes.addFlashAttribute("message", "Demande validee.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return redirectListe(utilisateur);
    }

    @PostMapping("/demandes/{id}/refuser")
    public String refuser(@UtilisateurActif Utilisateur utilisateur, @PathVariable int id,
                          @RequestParam(required = false) String motif,
                          RedirectAttributes redirectAttributes) {
        try {
            validationService.changer(id, StatutDemande.REFUSER,
                    utilisateurService.idEmploye(utilisateur), motif);
            redirectAttributes.addFlashAttribute("message", "Demande refusee.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return redirectListe(utilisateur);
    }

    @PostMapping("/demandes/{id}/revoir")
    public String revoir(@UtilisateurActif Utilisateur utilisateur, @PathVariable int id,
                         @RequestParam Map<String, String> parametres,
                         RedirectAttributes redirectAttributes) {
        try {
            String commentaire = parametres.getOrDefault("commentaire", "").trim();
            List<ValidationService.LigneRevision> lignes = lignesARevoir(id, parametres, commentaire);
            validationService.demanderRevision(id, utilisateurService.idEmploye(utilisateur),
                    commentaire, lignes);
            redirectAttributes.addFlashAttribute("message", "Demande remise a revoir.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return redirectListe(utilisateur);
    }

    /* ------------------------------------------------------------------ */
    // Correction par le manager
    /* ------------------------------------------------------------------ */

    @PostMapping("/manager/corrections/{id}/decision")
    public String deciderCorrection(@UtilisateurActif Utilisateur utilisateur, @PathVariable int id,
                                    @RequestParam boolean accepter,
                                    RedirectAttributes redirectAttributes) {
        if (utilisateurService.role(utilisateur) != TypeRole.MANAGER) {
            redirectAttributes.addFlashAttribute("erreur", "Action reservee au manager.");
            return "redirect:/validation";
        }
        try {
            if (accepter) {
                correctionService.accepter(id, utilisateurService.idEmploye(utilisateur));
                redirectAttributes.addFlashAttribute("message", "Correction acceptee : la demande reprend son circuit.");
            } else {
                correctionService.abandonner(id, utilisateurService.idEmploye(utilisateur));
                redirectAttributes.addFlashAttribute("message", "Demande abandonnee.");
            }
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("erreur", exception.getMessage());
        }
        return "redirect:/validation/manager/corrections";
    }

    /* ------------------------------------------------------------------ */
    // Prives
    /* ------------------------------------------------------------------ */

    /** Redirection vers la liste correspondant au role connecte. */
    private String redirectListe(Utilisateur utilisateur) {
        return utilisateurService.role(utilisateur) == TypeRole.MANAGER
                ? "redirect:/validation/manager/corrections"
                : "redirect:/validation";
    }

    /**
     * Transforme les champs du formulaire de revision en lignes de correction.
     * Le formulaire envoie {@code effectif_<idDetail>} et
     * {@code commentaire_<idDetail>} pour chaque ligne concernee.
     */
    private List<ValidationService.LigneRevision> lignesARevoir(int idDemande,
                                                                 Map<String, String> parametres,
                                                                 String commentaireGeneral) {
        List<ValidationService.LigneRevision> lignes = new ArrayList<>();
        for (var ligne : validationService.lignes(idDemande)) {
            String effectifBrut = parametres.get("effectif_" + ligne.getId());
            String commentaireLigne = parametres.get("commentaire_" + ligne.getId());

            Integer effectif = null;
            if (effectifBrut != null && !effectifBrut.isBlank()) {
                try {
                    effectif = Integer.valueOf(effectifBrut.trim());
                } catch (NumberFormatException exception) {
                    throw new IllegalArgumentException("Effectif invalide pour la ligne " + ligne.getId());
                }
                if (effectif < 1) {
                    throw new IllegalArgumentException("Effectif invalide pour la ligne " + ligne.getId());
                }
            }
            String commentaire = commentaireLigne != null && !commentaireLigne.isBlank()
                    ? commentaireLigne.trim() : commentaireGeneral;

            if (effectif != null || !commentaire.isBlank()) {
                lignes.add(new ValidationService.LigneRevision(ligne.getId(), effectif));
            }
        }
        return lignes;
    }
}
