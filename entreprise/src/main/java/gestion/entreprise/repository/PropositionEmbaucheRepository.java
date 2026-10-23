package gestion.entreprise.repository;

import gestion.entreprise.modele.PropositionEmbauche;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropositionEmbaucheRepository extends JpaRepository<PropositionEmbauche, Integer> {
    List<PropositionEmbauche> findByCandidatId(int idCandidat);
    PropositionEmbauche findFirstByCandidatIdOrderByCreatedAtDesc(int idCandidat);
}