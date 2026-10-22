package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiplomeService {
    @Autowired 
    private DiplomeRepository diplomeRepository;

    public List<Diplome> getAll() {
        return diplomeRepository.findAll();
    }
}