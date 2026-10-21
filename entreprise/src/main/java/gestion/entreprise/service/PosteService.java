package gestion.entreprise.service;

import gestion.entreprise.modele.Poste;
import gestion.entreprise.repository.PosteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PosteService {

    private final PosteRepository posteRepository;

    public PosteService(PosteRepository posteRepository) {
        this.posteRepository = posteRepository;
    }

    public List<Poste> getAll() {
        return posteRepository.findAll();
    }

    public List<Poste> getByDepartement(int idDepartement) {
        return posteRepository.findByDepartement_Id(idDepartement);
    }
}