package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import gestion.entreprise.vue.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DemandeController {

    @Autowired
    private HistoriqueDemandeStatusService historiqueDemandeStatusService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private PosteRepository posteRepository;

    @Autowired
    private DepartementRepository departementRepository;

    @Autowired
    private DiplomeRepository diplomeRepository;

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    @Autowired
    private DemandeARevoirRepository demandeARevoirRepository;

    @Autowired
    private HistoriqueBudgetAnnuelRepository historiqueBudgetAnnuelRepository;

    /**
     * GET /{roleSigle}/accueil?status=XX&dateMin=YYYY-MM-DD&dateMax=YYYY-MM-DD
     * UN seul accueil pour tous les rôles, avec if/else.
     * Workflow:
     *   MANAGER → ses propres demandes
     *   RH → demandes avec dernier statut CR par un MANAGER
     *   FINANCE → demandes avec dernier statut VA par un RH
     *   DG → demandes avec dernier statut VA par FINANCE
     */
    @GetMapping("/{roleSigle}/accueil")
    public String accueil(@PathVariable String roleSigle,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String dateMin,
                          @RequestParam(required = false) String dateMax,
                          Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné à cet utilisateur.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase(roleSigle)) {
                throw new RuntimeException("Accès non autorisé pour ce rôle.");
            }

            // Parser dates filtres
            LocalDate dMin = null;
            LocalDate dMax = null;
            if (dateMin != null && !dateMin.isEmpty()) {
                dMin = LocalDate.parse(dateMin);
            }
            if (dateMax != null && !dateMax.isEmpty()) {
                dMax = LocalDate.parse(dateMax);
            }

            List<HistoriqueDemandeStatusView> demandes;
            String sigleRole = utilisateur.getRole().getSigle().toUpperCase();

            if (sigleRole.equals("MANAGER")) {
                demandes = historiqueDemandeStatusService
                        .getDemandesByEmployer(utilisateur.getEmployer().getId());
            } else if (sigleRole.equals("RH")) {
                // RH voit les demandes créées par un MANAGER
                demandes = historiqueDemandeStatusService
                        .getDemandesByStatusAndRole("CR", "MANAGER");
            } else if (sigleRole.equals("FINANCE")) {
                // Finance voit les demandes validées par un RH
                demandes = historiqueDemandeStatusService
                        .getDemandesByStatusAndRole("VA", "RH");

                // Budget annuel actuel pour Finance
                HistoriqueBudgetAnnuel budgetActuel = historiqueBudgetAnnuelRepository
                        .findActiveAtDate(LocalDate.now());
                if (budgetActuel != null) {
                    model.addAttribute("budgetAnnuel", budgetActuel.getValeur());
                }
            } else if (sigleRole.equals("DG")) {
                // DG voit les demandes validées par la FINANCE
                demandes = historiqueDemandeStatusService
                        .getDemandesByStatusAndRole("VA", "FINANCE");
            } else {
                demandes = historiqueDemandeStatusService.getDemandesRecent();
            }

            // Filtrage serveur-side
            demandes = historiqueDemandeStatusService.filtrer(demandes, status, dMin, dMax);

            model.addAttribute("demandes", demandes);
            model.addAttribute("roleSigle", roleSigle.toLowerCase());
            model.addAttribute("filterStatus", status);
            model.addAttribute("filterDateMin", dateMin);
            model.addAttribute("filterDateMax", dateMax);

            return "demandes/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * GET /{roleSigle}/demandes/{idDemande}
     * UN seul détail. Manager = seulement abandonner. Finance = budget.
     */
    @GetMapping("/{roleSigle}/demandes/{idDemande}")
    public String detail(@PathVariable String roleSigle,
                         @PathVariable int idDemande,
                         Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné à cet utilisateur.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase(roleSigle)) {
                throw new RuntimeException("Accès non autorisé pour ce rôle.");
            }

            Demande demande = demandeService.getById(idDemande);
            List<DetailDemande> details = detailDemandeRepository.findByDemandeId(idDemande);
            List<HistoriqueDemandeStatusView> historiques = historiqueDemandeStatusService.getByDemande(idDemande);

            model.addAttribute("demande", demande);
            model.addAttribute("details", details);
            model.addAttribute("historiques", historiques);
            model.addAttribute("roleSigle", roleSigle.toLowerCase());

            String sigleRole = utilisateur.getRole().getSigle().toUpperCase();

            // Dernier statut
            HistoriqueDemandeStatusView dernierStatut = null;
            if (!historiques.isEmpty()) {
                dernierStatut = historiques.get(historiques.size() - 1);
            }

            // Manager: s'il y a des corrections "à revoir", on les affiche
            boolean estARevoir = false;
            if (sigleRole.equals("MANAGER") && dernierStatut != null
                    && dernierStatut.getSigle().equalsIgnoreCase("AR")) {
                estARevoir = true;
                List<DemandeARevoir> corrections = new ArrayList<>();
                for (DetailDemande detail : details) {
                    List<DemandeARevoir> revisions = demandeARevoirRepository
                            .findByDemandeDetailsId(detail.getId());
                    for (DemandeARevoir rev : revisions) {
                        corrections.add(rev);
                    }
                }
                model.addAttribute("corrections", corrections);
            }
            model.addAttribute("estARevoir", estARevoir);

            // Finance: calcul du montant et vérification du budget
            if (sigleRole.equals("FINANCE")) {
                double montant = 0;
                boolean respecteBudget = false;
                String erreurBudget = null;

                try {
                    montant = demandeService.getMontantDemande(demande);
                    respecteBudget = demandeService.respecteLeBudget(demande);
                } catch (Exception e) {
                    erreurBudget = e.getMessage();
                }

                model.addAttribute("montant", montant);
                model.addAttribute("respecteBudget", respecteBudget);
                model.addAttribute("erreurBudget", erreurBudget);
                model.addAttribute("afficherBudget", true);
            } else {
                model.addAttribute("afficherBudget", false);
            }

            return "demandes/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/" + roleSigle.toLowerCase() + "/accueil";
        }
    }

    /**
     * GET /{roleSigle}/demandes/nouvelle
     * Formulaire de nouvelle demande (MANAGER uniquement).
     */
    @GetMapping("/{roleSigle}/demandes/nouvelle")
    public String nouvelleDemande(@PathVariable String roleSigle,
                                  Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase(roleSigle)) {
                throw new RuntimeException("Accès non autorisé.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase("MANAGER")) {
                throw new RuntimeException("Seuls les managers peuvent créer des demandes.");
            }

            model.addAttribute("demande", new Demande());
            model.addAttribute("roleSigle", roleSigle.toLowerCase());

            return "demandes/nouvelle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/" + roleSigle.toLowerCase() + "/accueil";
        }
    }

    /**
     * POST /{roleSigle}/demandes
     * Sauvegarde de la demande (MANAGER uniquement).
     */
    @PostMapping("/{roleSigle}/demandes")
    public String sauvegarderDemande(@PathVariable String roleSigle,
                                     Model model, HttpSession session,
                                     RedirectAttributes redirectAttributes, Demande demande) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase("MANAGER")) {
                throw new RuntimeException("Seuls les managers peuvent créer des demandes.");
            }

            List<DetailDemande> detailDemandes = demande.getdetailDemandes();

            if (detailDemandes == null || detailDemandes.isEmpty()) {
                throw new RuntimeException("Vous devez ajouter au moins un poste.");
            }

            if (demande.getDateLimite() == null) {
                throw new RuntimeException("La date limite est obligatoire.");
            }

            if (demande.getDateLimite().isBefore(LocalDate.now())) {
                throw new RuntimeException("La date limite ne peut pas être dans le passé.");
            }

            demande.setEmployer(utilisateur.getEmployer());

            for (DetailDemande detailDemande : detailDemandes) {
                Poste poste = posteRepository.findById(detailDemande.getPoste().getId()).orElse(null);
                if (poste == null) {
                    throw new RuntimeException("Poste introuvable.");
                }

                Departement departement = departementRepository.findById(detailDemande.getDepartement().getId()).orElse(null);
                if (departement == null) {
                    throw new RuntimeException("Département introuvable.");
                }

                Diplome diplome = diplomeRepository.findById(detailDemande.getDiplome().getId()).orElse(null);
                if (diplome == null) {
                    throw new RuntimeException("Diplôme introuvable.");
                }

                if (detailDemande.getAnneeExperience() < 0) {
                    throw new RuntimeException("L'annee d'expérience ne peut pas être négative.");
                }

                if (detailDemande.getNbrPersonnel() <= 0) {
                    throw new RuntimeException("Le nombre de personnel doit être strictement positif.");
                }

                detailDemande.setDemande(demande);
                detailDemande.setPoste(poste);
                detailDemande.setDepartement(departement);
                detailDemande.setDiplome(diplome);
            }

            demandeService.save(demande);

            redirectAttributes.addFlashAttribute("succes", "Demande créée avec succès.");
            return "redirect:/" + roleSigle.toLowerCase() + "/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/" + roleSigle.toLowerCase() + "/demandes/nouvelle";
        }
    }

    /**
     * GET /{roleSigle}/demandes/{idDemande}/correction
     * Page de correction (comme nouvelle demande mais pré-rempli, seul l'effectif est modifiable).
     * Tout rôle sauf MANAGER peut y accéder.
     */
    @GetMapping("/{roleSigle}/demandes/{idDemande}/correction")
    public String pageCorrection(@PathVariable String roleSigle,
                                 @PathVariable int idDemande,
                                 Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné.");
            }

            if (!utilisateur.getRole().getSigle().equalsIgnoreCase(roleSigle)) {
                throw new RuntimeException("Accès non autorisé.");
            }

            Demande demande = demandeService.getById(idDemande);
            List<DetailDemande> details = detailDemandeRepository.findByDemandeId(idDemande);

            model.addAttribute("demande", demande);
            model.addAttribute("details", details);
            model.addAttribute("roleSigle", roleSigle.toLowerCase());

            return "demandes/correction";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/" + roleSigle.toLowerCase() + "/demandes/" + idDemande;
        }
    }

    /**
     * POST /{roleSigle}/demandes/{idDemande}/correction
     * Soumet la correction. Insert dans demande_a_revoir + change statut à AR.
     * Seul l'effectif est accepté côté serveur.
     */
    @PostMapping("/{roleSigle}/demandes/{idDemande}/correction")
    public String soumettreCorrection(@PathVariable String roleSigle,
                                      @PathVariable int idDemande,
                                      @RequestParam String motif,
                                      @RequestParam("detailId") List<Integer> detailIds,
                                      @RequestParam("nbSouhaite") List<Integer> nbSouhaites,
                                      Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Utilisateur utilisateur = utilisateurService.authentifierUtilisateur(session, model);

            if (utilisateur.getRole() == null) {
                throw new RuntimeException("Aucun rôle assigné.");
            }

            if (utilisateur.getEmployer() == null) {
                throw new RuntimeException("Utilisateur sans employé associé.");
            }

            if (motif == null || motif.trim().isEmpty()) {
                throw new RuntimeException("Le motif est obligatoire.");
            }

            Demande demande = demandeService.getById(idDemande);

            // Changer le statut à AR
            Status statusAR = historiqueDemandeStatusService.getStatusBySigle("AR");
            if (statusAR == null) {
                throw new RuntimeException("Statut 'A Revoir' introuvable.");
            }

            historiqueDemandeStatusService.changerStatut(demande, statusAR, utilisateur.getEmployer(), motif.trim());

            // Insérer les snapshots de correction (seul l'effectif)
            if (detailIds != null) {
                for (int i = 0; i < detailIds.size(); i++) {
                    int detailId = detailIds.get(i);
                    int nbSouhaite = nbSouhaites.get(i);

                    if (nbSouhaite <= 0) {
                        throw new RuntimeException("L'effectif souhaité doit être strictement positif.");
                    }

                    DetailDemande detail = detailDemandeRepository.findById(detailId).orElse(null);
                    if (detail == null) {
                        throw new RuntimeException("Détail de demande introuvable: " + detailId);
                    }

                    // Vérifier que le detail appartient bien à cette demande
                    if (detail.getDemande().getId() != idDemande) {
                        throw new RuntimeException("Ce détail n'appartient pas à cette demande.");
                    }

                    DemandeARevoir correction = new DemandeARevoir();
                    correction.setDemandeur(utilisateur.getEmployer());
                    correction.setDemandeDetails(detail);
                    correction.setNbSouhaite(nbSouhaite);

                    demandeARevoirRepository.save(correction);
                }
            }

            redirectAttributes.addFlashAttribute("succes", "Correction soumise avec succès.");
            return "redirect:/" + roleSigle.toLowerCase() + "/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/" + roleSigle.toLowerCase() + "/demandes/" + idDemande + "/correction";
        }
    }
}
