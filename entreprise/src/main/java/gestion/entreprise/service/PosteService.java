package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PosteService {

    @Autowired
    private PosteRepository posteRepository;

    public List<Poste> getAll() {
        return posteRepository.findAll();
    }

    public List<Poste> getByDepartement(int idDepartement) {
        return posteRepository.findByDepartementId(idDepartement);
    }
}