package gestion.entreprise.service;

import gestion.entreprise.modele.Diplome;
import gestion.entreprise.repository.DiplomeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiplomeService {

    @Autowired
    private DiplomeRepository diplomeRepository;

    /**
     * Backoffice / REST : tous les diplômes.
     * Appelé par DiplomeRestController et nouvelle-demande.js.
     */
    public List<Diplome> getAll() {
        return diplomeRepository.findAll();
    }

    /**
     * Frontoffice : alias historique de getAll().
     * Appelé par les contrôleurs publics du frontoffice.
     */
    public List<Diplome> getAllDiplomes() {
        return diplomeRepository.findAll();
    }
}