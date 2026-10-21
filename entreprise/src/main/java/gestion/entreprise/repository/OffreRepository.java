package gestion.entreprise.repository;

import gestion.entreprise.modele.Offre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffreRepository extends JpaRepository<Offre, Integer> {
    boolean existsByDetailDemande_Id(int idDetailDemande);
}
