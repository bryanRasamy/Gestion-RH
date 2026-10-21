package gestion.entreprise.repository;

import gestion.entreprise.modele.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    List<Parametre> findByCodeAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(String code, LocalDate date);
}
