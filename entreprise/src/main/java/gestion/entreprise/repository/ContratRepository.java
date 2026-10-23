package gestion.entreprise.repository;

import gestion.entreprise.modele.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    Contrat findFirstByPropositionId(int idProposition);
}