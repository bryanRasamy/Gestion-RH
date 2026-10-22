package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status findBySigle(String sigle);
}