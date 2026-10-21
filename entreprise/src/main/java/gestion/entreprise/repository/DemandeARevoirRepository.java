package gestion.entreprise.repository;

import gestion.entreprise.modele.DemandeARevoir;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeARevoirRepository extends JpaRepository<DemandeARevoir, Integer> {
	List<DemandeARevoir> findByIdDemandeurAndTraiteFalseOrderByDateAsc(int idDemandeur);
	List<DemandeARevoir> findByTraiteFalseOrderByDateAsc();
	DemandeARevoir findTopByIdDemandeDetailsAndTraiteFalseOrderByDateDesc(int idDemandeDetails);
	List<DemandeARevoir> findByIdDemandeDetailsInAndTraiteFalse(List<Integer> idsDetails);
	DemandeARevoir findOneById(int id);
}
