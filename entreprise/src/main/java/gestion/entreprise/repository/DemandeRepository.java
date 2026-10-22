package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    boolean existsByMatricule(String matricule);

    List<Demande> findByEmployerId(int idEmployer);

    @Query(value = "SELECT DISTINCT d.* FROM demande d " +
            "JOIN historique_demande_status h ON h.id_demande = d.id " +
            "JOIN (SELECT id_demande, MAX(date) as max_date FROM historique_demande_status GROUP BY id_demande) latest " +
            "ON h.id_demande = latest.id_demande AND h.date = latest.max_date " +
            "WHERE h.id_status = :idStatus", nativeQuery = true)
    List<Demande> findByCurrentStatus(@Param("idStatus") int idStatus);

    @Query(value = "SELECT DISTINCT d.* FROM demande d " +
            "JOIN historique_demande_status h ON h.id_demande = d.id " +
            "JOIN (SELECT id_demande, MAX(date) as max_date FROM historique_demande_status GROUP BY id_demande) latest " +
            "ON h.id_demande = latest.id_demande AND h.date = latest.max_date " +
            "WHERE h.id_status = :idStatus AND h.id_employer = :idEmployer", nativeQuery = true)
    List<Demande> findByCurrentStatusAndEvaluateur(@Param("idStatus") int idStatus, @Param("idEmployer") int idEmployer);
}