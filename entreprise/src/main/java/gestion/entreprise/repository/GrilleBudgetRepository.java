package gestion.entreprise.repository;

import gestion.entreprise.modele.GrilleBudget;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrilleBudgetRepository extends JpaRepository<GrilleBudget, Integer> {
    GrilleBudget findFirstByPoste_IdAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(int idPoste, LocalDate date);
}
