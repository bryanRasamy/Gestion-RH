package gestion.entreprise.repository;

import gestion.entreprise.vue.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoriqueDemandeStatusViewRepository extends JpaRepository<HistoriqueDemandeStatusView, Integer> {

    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "ORDER BY id_demande, date DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemand();

    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "WHERE id_employer = :idEmployer " +
            "ORDER BY id_demande, date DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemandByEmployer(@Param("idEmployer") int idEmployer);

    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "WHERE sigle = :sigle " +
            "ORDER BY id_demande, date DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemandBySigle(@Param("sigle") String sigle);

    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "WHERE sigle = :sigle AND id_employer_evaluateur = :idEmployerEvaluateur " +
            "ORDER BY id_demande, date DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemandBySigleAndEvaluateur(@Param("sigle") String sigle, @Param("idEmployerEvaluateur") int idEmployerEvaluateur);

    @Query(value = "SELECT id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status WHERE id_demande = :idDemande ORDER BY date ASC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findAllByDemande(@Param("idDemande") int idDemande);
}
