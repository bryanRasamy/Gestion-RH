package gestion.entreprise.repository;

import gestion.entreprise.modele.ContratStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratStatutRepository extends JpaRepository<ContratStatut, Integer> {
    ContratStatut findByLibelle(String libelle);
}
