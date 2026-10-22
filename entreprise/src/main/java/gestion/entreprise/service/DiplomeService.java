package gestion.entreprise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.entreprise.modele.Diplome;
import gestion.entreprise.repository.DiplomeRepository;

@Service
public class DiplomeService {

    @Autowired
    private DiplomeRepository diplomeRepository;

    public List<Diplome> getAllDiplomes() {
        return diplomeRepository.findAll();
    }
}
