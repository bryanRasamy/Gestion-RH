package gestion.entreprise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

@Service
public class PosteService {

    @Autowired
    private PosteRepository posteRepository;

    public List<Poste> getAllPoste() {
        return posteRepository.findAll();
    }
}
