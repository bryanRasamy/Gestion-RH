package gestion.entreprise.repository;

import gestion.entreprise.modele.Parametre;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    Parametre findFirstByCodeAndDateEffectiveLessThanEqualOrderByDateEffectiveDesc(String code, LocalDate date);
}
