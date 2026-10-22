package gestion.entreprise.service;

import gestion.entreprise.modele.DemandeARevoir;
import gestion.entreprise.repository.DemandeARevoirRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Consultation des lignes de detail demandees en revision (a revoir). */
@Service
public class DemandeARevoirService {

    private final DemandeARevoirRepository repository;

    public DemandeARevoirService(DemandeARevoirRepository repository) {
        this.repository = repository;
    }

    public List<DemandeARevoir> listerPourDemandeur(int idDemandeur) {
        return repository.findByIdDemandeurOrderByDateAsc(idDemandeur);
    }

    public List<DemandeARevoir> listerParDetails(List<Integer> idsDetails) {
        if (idsDetails == null || idsDetails.isEmpty()) {
            return List.of();
        }
        return repository.findByIdDemandeDetailsIn(idsDetails);
    }
}
