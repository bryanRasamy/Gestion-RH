package gestion.entreprise.repository;

import gestion.entreprise.modele.ContratType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratTypeRepository extends JpaRepository<ContratType, Integer> {
    ContratType findByLibelle(String libelle);
}