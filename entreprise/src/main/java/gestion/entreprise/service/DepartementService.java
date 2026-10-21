package gestion.entreprise.service;

import gestion.entreprise.modele.Departement;
import gestion.entreprise.repository.DepartementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartementService {

    private final DepartementRepository departementRepository;

    public DepartementService(DepartementRepository departementRepository) {
        this.departementRepository = departementRepository;
    }

    public List<Departement> getAll() {
        return departementRepository.findAll();
    }
}