package gestion.entreprise.repository;

import gestion.entreprise.modele.DemandeARevoir;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeARevoirRepository extends JpaRepository<DemandeARevoir, Integer> {
    List<DemandeARevoir> findAllByDetailDemande_Demande_IdAndTraiteFalse(int idDemande);
}
