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
@RequestMapping("/api/diplomes")
public class DiplomeRestController {
    @Autowired 
    private DiplomeService diplomeService;

    @CrossOrigin(origins = "*")
    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Diplome> diplomes = diplomeService.getAll();

        if(diplomes == null) return null;

        List<Map<String, Object>> response = new ArrayList<>();
 
        for (Diplome diplome : diplomes) {
            Map<String, Object> diplomeMap = new HashMap<>();
            diplomeMap.put("id", diplome.getId());
            diplomeMap.put("anneeEtude", diplome.getAnneeEtude());
            diplomeMap.put("nomDiplome", diplome.getNomDiplome());

            response.add(diplomeMap);
        }

        return response;
    }
}