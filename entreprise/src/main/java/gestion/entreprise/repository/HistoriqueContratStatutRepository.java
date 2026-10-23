package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueContratStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueContratStatutRepository extends JpaRepository<HistoriqueContratStatut, Integer> {
    List<HistoriqueContratStatut> findByContratIdOrderByDateChangementAsc(int idContrat);
}
