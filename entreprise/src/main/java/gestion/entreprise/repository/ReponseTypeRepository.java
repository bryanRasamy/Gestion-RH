package gestion.entreprise.repository;

import gestion.entreprise.modele.ReponseType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReponseTypeRepository extends JpaRepository<ReponseType, Integer> {
    ReponseType findByLibelle(String libelle);
}