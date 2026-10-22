package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {

    List<HistoriqueDemandeStatus> findByDemandeId(int idDemande);

    @Query(value = "SELECT * FROM historique_demande_status WHERE id_demande = :idDemande ORDER BY date DESC, id DESC LIMIT 1", nativeQuery = true)
    HistoriqueDemandeStatus findLatestByDemande(@Param("idDemande") int idDemande);

    @Query(value = "SELECT * FROM historique_demande_status WHERE id_demande = :idDemande ORDER BY date ASC, id ASC LIMIT 1", nativeQuery = true)
    HistoriqueDemandeStatus findFirstByDemande(@Param("idDemande") int idDemande);

    /**
     * Anti-doublon : un même employé ne peut pas poser deux fois le même statut
     * sur la même demande. Protège contre le double-clic et les rejeux réseau.
     * Doit être renforcé par une contrainte UNIQUE en base (cf. script SQL).
     */
    boolean existsByDemandeIdAndStatusIdAndEmployerId(int idDemande, int idStatus, int idEmployer);
}