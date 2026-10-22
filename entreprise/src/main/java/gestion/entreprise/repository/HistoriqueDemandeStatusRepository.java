package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {
    Optional<HistoriqueDemandeStatus> findFirstByDemandeIdOrderByIdDesc(int demandeId);
}
