package gestion.entreprise.api;

import gestion.entreprise.modele.Poste;
import gestion.entreprise.service.PosteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/postes")
public class PosteRestController {

    private final PosteService posteService;

    public PosteRestController(PosteService posteService) {
        this.posteService = posteService;
    }

    @GetMapping
    public List<Poste> getAll(@RequestParam(name = "idDepartement", required = false) Integer idDepartement) {
        if (idDepartement == null) {
            return posteService.getAll();
        }
        return posteService.getByDepartement(idDepartement);
    }
}