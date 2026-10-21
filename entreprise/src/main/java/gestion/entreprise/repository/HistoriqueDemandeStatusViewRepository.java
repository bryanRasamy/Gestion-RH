package gestion.entreprise.repository;

import gestion.entreprise.vue.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HistoriqueDemandeStatusViewRepository extends JpaRepository<HistoriqueDemandeStatusView, Integer> {

    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, date " +
            "FROM v_historique_demande_status " +
            "ORDER BY id_demande, date DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemand();
}
