package gestion.entreprise.repository;

import gestion.entreprise.modele.GrilleBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrilleBudgetRepository extends JpaRepository<GrilleBudget, Integer> {

    @Query(value = "SELECT * FROM grille_budget WHERE id_poste = :idPoste AND date_effective <= :dateRef ORDER BY date_effective DESC LIMIT 1", nativeQuery = true)
    GrilleBudget findActiveByPosteAndDate(@Param("idPoste") int idPoste, @Param("dateRef") java.time.LocalDateTime dateRef);
}
