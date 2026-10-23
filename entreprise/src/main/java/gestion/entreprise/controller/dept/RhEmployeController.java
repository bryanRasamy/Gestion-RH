package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import gestion.entreprise.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class RhEmployeController {

    @Autowired private UtilisateurService utilisateurService;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private PeriodeEmploiRepository periodeEmploiRepository;
    @Autowired private ContratRepository contratRepository;
    @Autowired private PaiePeriodeRepository paiePeriodeRepository;

    @GetMapping("/rh/employes")
    public String listeEmployes(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);

            List<Employer> employeurs = employerRepository.findAll();
            List<Map<String, Object>> employes = new ArrayList<>();

            for (Employer e : employeurs) {
                List<PeriodeEmploi> periodesActives = periodeEmploiRepository
                        .findAllByEmployerIdAndStatutLibelle(e.getId(), "ACTIVE");
                PeriodeEmploi periodeActive = periodesActives.isEmpty() ? null : periodesActives.get(0);

                Contrat contratActuel = null;
                if (periodeActive != null) {
                    List<Contrat> historiques = contratRepository.findHistoriqueByPeriodeEmploi(periodeActive.getId());
                    contratActuel = periodeActive.getContratActuel() != null
                            ? periodeActive.getContratActuel()
                            : historiques.isEmpty() ? null : historiques.get(historiques.size() - 1);
                }

                Map<String, Object> row = new HashMap<>();
                row.put("employer", e);
                row.put("periodeActive", periodeActive);
                row.put("contratActuel", contratActuel);
                employes.add(row);
            }

            model.addAttribute("employes", employes);
            model.addAttribute("titre", "Employés");
            model.addAttribute("menu", "employes");
            return "demandes/rh-employes";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/rh/employes/{id}")
    public String detailEmploye(@PathVariable int id, Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);

            Employer employer = employerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employé introuvable."));

            List<PeriodeEmploi> periodes = periodeEmploiRepository.findByEmployerIdOrderByDateDebutDesc(employer.getId());
            List<PeriodeEmploi> periodesActives = periodeEmploiRepository
                    .findAllByEmployerIdAndStatutLibelle(employer.getId(), "ACTIVE");
            PeriodeEmploi periodeActive = periodesActives.isEmpty() ? null : periodesActives.get(0);

            Contrat contratActuel = null;
            if (periodeActive != null) {
                List<Contrat> historiques = contratRepository.findHistoriqueByPeriodeEmploi(periodeActive.getId());
                contratActuel = periodeActive.getContratActuel() != null
                        ? periodeActive.getContratActuel()
                        : historiques.isEmpty() ? null : historiques.get(historiques.size() - 1);
            }

            List<Contrat> contrats = contratRepository.findAllByEmployerOrderByDateDebutDesc(employer.getId());
            List<PaiePeriode> paies = paiePeriodeRepository.findByEmployerIdOrderByAnneeDescMoisDesc(employer.getId());

            model.addAttribute("employer", employer);
            model.addAttribute("periodes", periodes);
            model.addAttribute("periodeActive", periodeActive);
            model.addAttribute("contratActuel", contratActuel);
            model.addAttribute("contrats", contrats);
            model.addAttribute("paies", paies);
            model.addAttribute("titre", "Détail employé");
            model.addAttribute("menu", "employes");
            return "demandes/rh-employe-detail";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/employes";
        }
    }

    @GetMapping("/rh/paie")
    public String etatPaie(Model model, HttpSession session, RedirectAttributes ra) {
        try {
            Utilisateur u = utilisateurService.authentifierUtilisateur(session, model);
            verifierRh(u);

            LocalDate maintenant = LocalDate.now();
            int anneeCourante = maintenant.getYear();
            int moisCourant = maintenant.getMonthValue();

            List<Employer> employeurs = employerRepository.findAll();
            List<Map<String, Object>> lignes = new ArrayList<>();

            for (Employer e : employeurs) {
                List<PeriodeEmploi> periodesActives = periodeEmploiRepository
                        .findAllByEmployerIdAndStatutLibelle(e.getId(), "ACTIVE");
                if (periodesActives.isEmpty()) {
                    continue;
                }

                PaiePeriode paie = null;
                List<PaiePeriode> historiques = paiePeriodeRepository.findByEmployerIdOrderByAnneeDescMoisDesc(e.getId());
                for (PaiePeriode p : historiques) {
                    if (p.getAnnee() == anneeCourante && p.getMois() == moisCourant) {
                        paie = p;
                        break;
                    }
                }

                Map<String, Object> ligne = new HashMap<>();
                ligne.put("employer", e);
                ligne.put("paie", paie);
                ligne.put("estPaye", paie != null);
                lignes.add(ligne);
            }

            model.addAttribute("lines", lignes);
            model.addAttribute("titre", "État de paie périodique");
            model.addAttribute("menu", "paie");
            return "demandes/rh-paie";
        } catch (Exception e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    private void verifierRh(Utilisateur u) throws Exception {
        if (u == null || u.getRole() == null || !"RH".equalsIgnoreCase(u.getRole().getSigle())) {
            throw new Exception("Accès réservé aux RH");
        }
    }
}
