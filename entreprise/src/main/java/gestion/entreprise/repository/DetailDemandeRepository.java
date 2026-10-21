package gestion.entreprise.repository;

import gestion.entreprise.modele.DetailDemande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {
    DetailDemande findFirstById(int id);
    List<DetailDemande> findAllByDemande_Id(int idDemande);
}
