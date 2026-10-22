package gestion.entreprise.service;

import gestion.entreprise.modele.Diplome;
import gestion.entreprise.repository.DiplomeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Referentiel des diplomes proposes dans une demande. */
@Service
public class DiplomeService {

    private final DiplomeRepository diplomeRepository;

    public DiplomeService(DiplomeRepository diplomeRepository) {
        this.diplomeRepository = diplomeRepository;
    }

    public List<Diplome> lister() {
        return diplomeRepository.findAll();
    }

    /** Diplome par defaut lorsqu'une ligne de demande n'en precise aucun. */
    public int idDiplomeParDefaut() {
        return diplomeRepository.findAll().stream()
                .map(Diplome::getId)
                .min(Integer::compareTo)
                .orElse(0);
    }
}
