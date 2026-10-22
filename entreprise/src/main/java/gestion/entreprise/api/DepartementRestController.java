package gestion.entreprise.api;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departements")
public class DepartementRestController {
    @Autowired 
    private DepartementService departementService;

    @CrossOrigin(origins = "*")
    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Departement> departements = departementService.getAll();

        if(departements == null) return null;

        List<Map<String, Object>> response = new ArrayList<>();
 
        for (Departement departement : departements) {
            Map<String, Object> departementMap = new HashMap<>();
            departementMap.put("id", departement.getId());
            departementMap.put("libelle", departement.getLibelle());
            departementMap.put("sigle", departement.getSigle());

            response.add(departementMap);
        }

        return response;
    }
}