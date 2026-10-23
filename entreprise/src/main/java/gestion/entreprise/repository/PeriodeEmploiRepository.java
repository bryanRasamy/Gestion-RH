package gestion.entreprise.repository;

import gestion.entreprise.modele.PeriodeEmploi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodeEmploiRepository extends JpaRepository<PeriodeEmploi, Integer> {
    PeriodeEmploi findFirstByEmployerIdOrderByIdDesc(int idEmployer);
}
