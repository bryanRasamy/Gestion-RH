package gestion.entreprise.repository;

import gestion.entreprise.modele.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AffectationRepository extends JpaRepository<Affectation, Integer> {
    List<Affectation> findByPeriodeEmploiId(int idPeriodeEmploi);
}
