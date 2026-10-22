package gestion.entreprise.repository;

import gestion.entreprise.modele.TestTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestTechniqueRepository extends JpaRepository<TestTechnique, Integer> {

    @Query("select t from TestTechnique t where t.poste.id = :idPoste and t.niveau.niveau = :niveau and t.actif = true")
    TestTechnique findActifByPosteAndNiveau(@Param("idPoste") int idPoste, @Param("niveau") int niveau);
}