package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueDemandeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {
	HistoriqueDemandeStatus findTopByIdDemandeOrderByIdDesc(int idDemande);
	List<HistoriqueDemandeStatus> findByIdDemandeOrderByIdAsc(int idDemande);
	List<HistoriqueDemandeStatus> findByLibelleOrderByDateAsc(String libelle);
}