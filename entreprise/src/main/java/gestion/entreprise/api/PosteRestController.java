package gestion.entreprise.api;

import gestion.entreprise.modele.*;
import gestion.entreprise.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/postes")
public class PosteRestController {

    @Autowired 
    private PosteService posteService;

    @CrossOrigin(origins = "*")
    @GetMapping
    public List<Map<String, Object>> getAll(@RequestParam(name = "idDepartement", required = false) int idDepartement) {
        List<Poste> postes = new ArrayList<>();

        if (idDepartement == 0) {
            postes=posteService.getAll();
        } else {
            postes=posteService.getByDepartement(idDepartement);
        }

        if(postes == null) return null;

        List<Map<String, Object>> response = new ArrayList<>();
 
        for (Poste poste : postes) {
            Map<String, Object> posteMap = new HashMap<>();
            posteMap.put("id", poste.getId());
            posteMap.put("idDepartment",poste.getDepartement().getId());
            posteMap.put("libelle", poste.getLibelle());
            posteMap.put("sigle", poste.getSigle());

            response.add(posteMap);
        }

        return response;
    }
}