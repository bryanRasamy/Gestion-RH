package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueBudgetAnnuel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface HistoriqueBudgetAnnuelRepository extends JpaRepository<HistoriqueBudgetAnnuel, Integer> {
    HistoriqueBudgetAnnuel findTopByDateEffectiveLessThanEqualOrderByDateEffectiveDesc(LocalDate date);
}