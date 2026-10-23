package gestion.entreprise.repository;

import gestion.entreprise.modele.OnboardingTacheType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnboardingTacheTypeRepository extends JpaRepository<OnboardingTacheType, Integer> {
    List<OnboardingTacheType> findByActifTrueOrderByOrdre();
}
