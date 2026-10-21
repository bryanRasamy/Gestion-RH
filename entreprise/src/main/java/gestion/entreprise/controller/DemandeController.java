package gestion.entreprise.controller;

import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/demandes")
public class DemandeController {
    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;

    public DemandeController(DemandeRepository demandeRepository, DetailDemandeRepository detailRepository,
                             HistoriqueDemandeStatusRepository historiqueRepository) {
        this.demandeRepository = demandeRepository;
        this.detailRepository = detailRepository;
        this.historiqueRepository = historiqueRepository;
    }

    @GetMapping("/nouvelle")
    public String nouvelleDemande() {
        return "demande/nouvelle";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        model.addAttribute("demande", demandeRepository.findOneById(id));
        model.addAttribute("details", detailRepository.findByIdDemande(id));
        model.addAttribute("historique", historiqueRepository.findByIdDemandeOrderByIdAsc(id));
        return "demande/detail";
    }
}