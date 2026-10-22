package gestion.entreprise.controller.dept;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.DepartementRepository;
import gestion.entreprise.repository.DiplomeRepository;
import gestion.entreprise.repository.PosteRepository;
import gestion.entreprise.service.*;
import gestion.entreprise.vue.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departements")
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



    @GetMapping("/accueil")
    public String getListeDemande(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.authentifierUtilisateur(session, model);

            List<HistoriqueDemandeStatusView> demandes = historiqueDemandeStatusService.getDemandesRecent();

            model.addAttribute("demandes", demandes);

            return "departements/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/demandes/nouvelle")
    public String nouvelleDemande(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            utilisateurService.authentifierUtilisateur(session, model);

            model.addAttribute("demande", new Demande());

            return "departements/nouvelle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/accueil";
        }
        
    }

    @PostMapping("/demandes")
    public String sauvegarderDemande(Model model, HttpSession session, RedirectAttributes redirectAttributes, Demande demande) {
        try {
            Utilisateur utilisateur=utilisateurService.authentifierUtilisateur(session, model);

            List<DetailDemande> detailDemandes = demande.getdetailDemandes();
            if (detailDemandes == null || detailDemandes.isEmpty()) throw new RuntimeException("Ajoutez au moins un poste");

            /*Controle*/
            if(demande.getDateLimite() == null){
                throw new RuntimeException("La date limite est obligatoire");
            }

            if (demande.getDateLimite().isBefore(LocalDate.now())) {
                throw new RuntimeException("La date limite ne peut pas être dans le passé");
            }

            demande.setEmployer(utilisateur.getEmployer());
            
            for (DetailDemande detailDemande : detailDemandes) {
                if (detailDemande.getPoste() == null || detailDemande.getDepartement() == null || detailDemande.getDiplome() == null) {
                    throw new RuntimeException("Le département, le poste et le diplôme sont obligatoires");
                }
                Poste poste = posteRepository.findById(detailDemande.getPoste().getId()).orElseThrow(() -> new RuntimeException("Poste introuvable"));
                Departement departement = departementRepository.findById(detailDemande.getDepartement().getId()).orElseThrow(() -> new RuntimeException("Département introuvable"));
                Diplome diplome = diplomeRepository.findById(detailDemande.getDiplome().getId()).orElseThrow(() -> new RuntimeException("Diplôme introuvable"));

                if (poste.getDepartement().getId() != departement.getId()) {
                    throw new RuntimeException("Le poste doit appartenir au département sélectionné");
                }

                if (departement.getId() != utilisateur.getEmployer().getPoste().getDepartement().getId()) {
                    throw new RuntimeException("Vous ne pouvez créer une demande que pour votre département");
                }

                if (detailDemande.getAnneeExperience() < 0) {
                    throw new RuntimeException("L'annee d'experience ne peut pas etre negatif");
                }

                if (detailDemande.getNbrPersonnel()<=0) {
                    throw new RuntimeException("Le nombre de personnel doit etre strictement positif");
                }
    
                detailDemande.setDemande(demande);
                detailDemande.setPoste(poste);
                detailDemande.setDepartement(departement);
                detailDemande.setDiplome(diplome);
            }
                
            demandeService.save(demande);
            

            redirectAttributes.addFlashAttribute("succes", "Demande créée et transmise à RH.");
            return "redirect:/departements/accueil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/departements/demandes/nouvelle";
        }
    }
}
