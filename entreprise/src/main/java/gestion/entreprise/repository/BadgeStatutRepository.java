package gestion.entreprise.repository;

import gestion.entreprise.modele.BadgeStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeStatutRepository extends JpaRepository<BadgeStatut, Integer> {
    BadgeStatut findByLibelle(String libelle);
}
