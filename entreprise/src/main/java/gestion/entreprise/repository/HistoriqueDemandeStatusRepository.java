package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {

    List<HistoriqueDemandeStatus> findByDemandeId(int idDemande);

    @Query(value = "SELECT * FROM historique_demande_status WHERE id_demande = :idDemande ORDER BY date DESC LIMIT 1", nativeQuery = true)
    HistoriqueDemandeStatus findLatestByDemande(@Param("idDemande") int idDemande);
}