package gestion.entreprise.repository;

import gestion.entreprise.modele.PaiePeriode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiePeriodeRepository extends JpaRepository<PaiePeriode, Integer> {

    List<PaiePeriode> findByAnneeAndMoisOrderByEmployerMatricule(int annee, int mois);

    List<PaiePeriode> findByEmployerIdOrderByAnneeDescMoisDesc(int idEmployer);

    boolean existsByEmployerIdAndAnneeAndMois(int idEmployer, int annee, int mois);
}
