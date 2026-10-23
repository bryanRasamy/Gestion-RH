package gestion.entreprise.repository;

import gestion.entreprise.modele.OnboardingTache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnboardingTacheRepository extends JpaRepository<OnboardingTache, Integer> {
    List<OnboardingTache> findByPeriodeEmploiIdOrderByTypeOrdre(int idPeriodeEmploi);
}
