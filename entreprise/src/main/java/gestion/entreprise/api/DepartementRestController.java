package gestion.entreprise.api;

import gestion.entreprise.modele.Departement;
import gestion.entreprise.service.DepartementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departements")
public class DepartementRestController {

    private final DepartementService departementService;

    public DepartementRestController(DepartementService departementService) {
        this.departementService = departementService;
    }

    @GetMapping
    public List<Departement> getAll() {
        return departementService.getAll();
    }
}