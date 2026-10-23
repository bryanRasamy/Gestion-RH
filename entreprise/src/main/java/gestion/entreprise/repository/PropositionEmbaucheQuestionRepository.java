package gestion.entreprise.repository;

import gestion.entreprise.modele.PropositionEmbaucheQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropositionEmbaucheQuestionRepository extends JpaRepository<PropositionEmbaucheQuestion, Integer> {
    List<PropositionEmbaucheQuestion> findByPropositionIdOrderByOrdre(int idProposition);
}
