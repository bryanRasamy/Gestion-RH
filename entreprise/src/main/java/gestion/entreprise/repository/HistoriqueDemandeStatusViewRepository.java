package gestion.entreprise.repository;

import gestion.entreprise.vue.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoriqueDemandeStatusViewRepository extends JpaRepository<HistoriqueDemandeStatusView, Integer> {

    /**
     * Dernière ligne d'historique par demande (tous employeurs confondus).
     */
    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, " +
            "matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "ORDER BY id_demande, date DESC, id DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemand();

    /**
     * Dernière ligne d'historique pour un demandeur donné.
     */
    @Query(value = "SELECT DISTINCT ON (id_demande) " +
            "id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, " +
            "matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status " +
            "WHERE id_employer = :idEmployer " +
            "ORDER BY id_demande, date DESC, id DESC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestStatusPerDemandByEmployer(@Param("idEmployer") int idEmployer);

    /**
     * CORRIGÉ : on isole D'ABORD la dernière ligne par demande (sous-requête),
     * PUIS on filtre par sigle et évaluateur.
     *
     * Workflow attendu :
     *   RH      → dernières demandes avec sigle = CR posé par un MANAGER
     *   FINANCE → dernières demandes avec sigle = VA posé par un RH
     *   DG      → dernières demandes avec sigle = VA posé par un FINANCE
     *
     * Avant la correction : le WHERE s'appliquait sur TOUTES les lignes,
     * donc une demande déjà validée restait visible chez RH avec le libellé "Créée".
     */
    @Query(value = "WITH derniere AS ( " +
            "  SELECT DISTINCT ON (id_demande) " +
            "    id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, " +
            "    matricule, id_employer, libelle, sigle, motif, date " +
            "  FROM v_historique_demande_status " +
            "  ORDER BY id_demande, date DESC, id DESC " +
            ") " +
            "SELECT * FROM derniere " +
            "WHERE sigle = :sigle AND id_employer_evaluateur = ANY(:evaluateurIds) " +
            "ORDER BY id_demande",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findLatestByStatusAndEvaluateurs(
            @Param("sigle") String sigle,
            @Param("evaluateurIds") int[] evaluateurIds);

    @Query(value = "SELECT id, id_demande, id_status, id_employer_evaluateur, nom_evaluateur, prenom_evaluateur, " +
            "matricule, id_employer, libelle, sigle, motif, date " +
            "FROM v_historique_demande_status WHERE id_demande = :idDemande ORDER BY date ASC, id ASC",
            nativeQuery = true)
    List<HistoriqueDemandeStatusView> findAllByDemande(@Param("idDemande") int idDemande);
}