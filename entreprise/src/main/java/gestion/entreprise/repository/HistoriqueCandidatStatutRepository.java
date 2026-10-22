package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueCandidatStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoriqueCandidatStatutRepository extends JpaRepository<HistoriqueCandidatStatut, Integer> {
    List<HistoriqueCandidatStatut> findByCandidatIdOrderByDateChangementAsc(int idCandidat);
}