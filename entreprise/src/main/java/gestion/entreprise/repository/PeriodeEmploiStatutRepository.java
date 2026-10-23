package gestion.entreprise.repository;

import gestion.entreprise.modele.PeriodeEmploiStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodeEmploiStatutRepository extends JpaRepository<PeriodeEmploiStatut, Integer> {
    PeriodeEmploiStatut findByLibelle(String libelle);
}
