package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {
    List<DetailDemande> findByDemandeId(int idDemande);
}