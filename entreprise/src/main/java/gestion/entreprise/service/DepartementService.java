package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartementService {
    @Autowired 
    private DepartementRepository departementRepository;

    public List<Departement> getAll() {
        return departementRepository.findAll();
    }
}