package gestion.entreprise.repository;

import gestion.entreprise.modele.OnboardingTacheStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingTacheStatutRepository extends JpaRepository<OnboardingTacheStatut, Integer> {
    OnboardingTacheStatut findByLibelle(String libelle);
}
