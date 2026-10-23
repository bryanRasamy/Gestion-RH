package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriquePropositionEmbaucheStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoriquePropositionEmbaucheStatutRepository extends JpaRepository<HistoriquePropositionEmbaucheStatut, Integer> {
    List<HistoriquePropositionEmbaucheStatut> findByPropositionIdOrderByDateChangementAsc(int idProposition);
}