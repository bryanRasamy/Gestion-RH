package gestion.entreprise.repository;

import gestion.entreprise.modele.Demande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    boolean existsByMatricule(String matricule);
    List<Demande> findByDateLimiteBeforeAndTraiteFalse(LocalDate date);
    Demande findOneById(int id);
}   