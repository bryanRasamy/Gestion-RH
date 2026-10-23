package gestion.entreprise.repository;

import gestion.entreprise.modele.PropositionEmbaucheStatut;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropositionEmbaucheStatutRepository extends JpaRepository<PropositionEmbaucheStatut, Integer> {
    PropositionEmbaucheStatut findByLibelle(String libelle);
}