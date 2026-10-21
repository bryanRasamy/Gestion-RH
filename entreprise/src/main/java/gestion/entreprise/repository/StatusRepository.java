package gestion.entreprise.repository;

import gestion.entreprise.modele.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status findByLibelle(String libelle);
    Status findByLibelleIgnoreCase(String libelle);
    Status findBySigleIgnoreCase(String sigle);
}