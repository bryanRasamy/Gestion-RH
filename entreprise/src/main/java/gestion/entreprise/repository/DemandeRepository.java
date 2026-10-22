package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    boolean existsByMatricule(String matricule);
}   