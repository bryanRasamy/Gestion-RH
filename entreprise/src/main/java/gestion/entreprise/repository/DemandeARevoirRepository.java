package gestion.entreprise.repository;

import gestion.entreprise.modele.DemandeARevoir;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeARevoirRepository extends JpaRepository<DemandeARevoir, Integer> {
    List<DemandeARevoir> findByDemandeDetailsId(int idDemandeDetails);
    List<DemandeARevoir> findByDemandeDetailsDemandeId(int idDemande);
}
