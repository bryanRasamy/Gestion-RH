package gestion.entreprise.service;

import gestion.entreprise.modele.Poste;
import gestion.entreprise.repository.PosteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PosteService {

    @Autowired
    private PosteRepository posteRepository;

    /**
     * Backoffice / REST : tous les postes.
     * Appelé par PosteRestController quand idDepartement = 0.
     */
    public List<Poste> getAll() {
        return posteRepository.findAll();
    }

    /**
     * Postes filtrés par département.
     * Appelé par PosteRestController quand idDepartement > 0
     * → utilisé par nouvelle-demande.js pour peupler le dropdown.
     */
    public List<Poste> getByDepartement(int idDepartement) {
        return posteRepository.findByDepartementId(idDepartement);
    }

    /**
     * Frontoffice : alias historique de getAll().
     */
    public List<Poste> getAllPoste() {
        return posteRepository.findAll();
    }
}