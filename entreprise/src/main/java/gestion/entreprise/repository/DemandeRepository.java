package gestion.entreprise.repository;

import gestion.entreprise.modele.Demande;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    boolean existsByMatricule(String matricule);
    Demande findOneById(int id);
}
