package gestion.entreprise.repository;

import gestion.entreprise.modele.TestTechniqueQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestTechniqueQuestionRepository extends JpaRepository<TestTechniqueQuestion, Integer> {
    List<TestTechniqueQuestion> findByTestTechniqueIdAndActifTrueOrderByOrdre(int idTestTechnique);
}