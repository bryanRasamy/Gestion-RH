package gestion.entreprise.repository;

import gestion.entreprise.modele.TestTechniquePassage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestTechniquePassageRepository extends JpaRepository<TestTechniquePassage, Integer> {

    @Query("""
            select distinct p from TestTechniquePassage p
            join fetch p.testTechnique t
            join fetch t.niveau n
            where p.candidat.id = :idCandidat
            order by p.dateDebut
            """)
    List<TestTechniquePassage> findByCandidatIdOrderByDateDebut(@Param("idCandidat") int idCandidat);
}