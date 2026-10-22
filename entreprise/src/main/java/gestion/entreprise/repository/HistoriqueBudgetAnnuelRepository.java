package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueBudgetAnnuel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoriqueBudgetAnnuelRepository extends JpaRepository<HistoriqueBudgetAnnuel, Integer> {

    @Query(value = "SELECT * FROM historique_budget_annuel WHERE date_effective <= :dateRef ORDER BY date_effective DESC LIMIT 1", nativeQuery = true)
    HistoriqueBudgetAnnuel findActiveAtDate(@Param("dateRef") java.time.LocalDateTime dateRef);
}
