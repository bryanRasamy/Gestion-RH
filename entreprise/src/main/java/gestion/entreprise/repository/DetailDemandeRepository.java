package gestion.entreprise.repository;

import gestion.entreprise.modele.DetailDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {
	List<DetailDemande> findByIdDemande(int idDemande);
	DetailDemande findOneById(int id);
}