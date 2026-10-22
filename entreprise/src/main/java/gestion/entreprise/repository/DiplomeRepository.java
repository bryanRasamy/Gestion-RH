package gestion.entreprise.repository;

import gestion.entreprise.modele.Diplome;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiplomeRepository extends JpaRepository<Diplome, Integer> {
}