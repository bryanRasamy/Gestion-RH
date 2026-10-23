package gestion.entreprise.repository;

import gestion.entreprise.modele.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {
    Contrat findFirstByPropositionId(int idProposition);
    List<Contrat> findByStatutId(int idStatut);

}