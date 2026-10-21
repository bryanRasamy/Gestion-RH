package gestion.entreprise.repository;

import gestion.entreprise.modele.GrilleBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface GrilleBudgetRepository extends JpaRepository<GrilleBudget, Integer> {
    GrilleBudget findTopByIdPosteAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(int idPoste, LocalDate date);
}