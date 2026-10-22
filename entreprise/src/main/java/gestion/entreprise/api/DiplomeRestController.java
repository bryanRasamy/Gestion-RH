package gestion.entreprise.api;

import gestion.entreprise.modele.Diplome;
import gestion.entreprise.service.DiplomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diplomes")
public class DiplomeRestController {

    private final DiplomeService diplomeService;

    public DiplomeRestController(DiplomeService diplomeService) {
        this.diplomeService = diplomeService;
    }

    @GetMapping
    public List<Diplome> getAll() {
        return diplomeService.lister();
    }
}
