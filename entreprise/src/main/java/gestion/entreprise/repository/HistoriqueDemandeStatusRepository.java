package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueDemandeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {
    HistoriqueDemandeStatus findFirstByDemande_IdOrderByIdDesc(int idDemande);
    List<HistoriqueDemandeStatus> findAllByDemande_IdOrderByIdDesc(int idDemande);

}
