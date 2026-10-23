package gestion.entreprise.repository;

import gestion.entreprise.modele.AffectationStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffectationStatutRepository extends JpaRepository<AffectationStatut, Integer> {
    AffectationStatut findByLibelle(String libelle);
}
