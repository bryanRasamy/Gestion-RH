package gestion.entreprise.repository;

import gestion.entreprise.modele.DemandeARevoir;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Lignes de detail demandees en revision par un RH / Finance / DG. */
public interface DemandeARevoirRepository extends JpaRepository<DemandeARevoir, Integer> {

    List<DemandeARevoir> findByIdDemandeurOrderByDateAsc(int idDemandeur);

    List<DemandeARevoir> findByIdDemandeDetailsIn(List<Integer> idsDetails);

    void deleteAllByIdDemandeDetailsIn(List<Integer> idsDetails);
}
