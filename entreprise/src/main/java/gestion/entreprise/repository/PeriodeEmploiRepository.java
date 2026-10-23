package gestion.entreprise.repository;

import gestion.entreprise.modele.PeriodeEmploi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PeriodeEmploiRepository extends JpaRepository<PeriodeEmploi, Integer> {

    /** Toutes les périodes d'un employé (historique complet). */
    List<PeriodeEmploi> findByEmployerIdOrderByDateDebutDesc(int idEmployer);

    /** Période ACTIVE d'un employé (0 ou 1 normalement — on prend la plus récente si plusieurs). */
    @Query("""
        select pe from PeriodeEmploi pe
        join pe.statut s
        where pe.employer.id = :idEmployer and s.libelle = :libelleStatut
        order by pe.id desc
        """)
    Optional<PeriodeEmploi> findFirstByEmployerIdAndStatutLibelle(
            @Param("idEmployer") int idEmployer,
            @Param("libelleStatut") String libelleStatut);

    @Query("""
            select pe from PeriodeEmploi pe
            join pe.statut s
            where pe.employer.id = :idEmployer and s.libelle = :libelleStatut
            order by pe.id desc
            """)
    List<PeriodeEmploi> findAllByEmployerIdAndStatutLibelle(
            @Param("idEmployer") int idEmployer,
            @Param("libelleStatut") String libelleStatut);

    /** Récupère la période avec son contrat actuel chargé. */
    @Query("""
        select distinct pe from PeriodeEmploi pe
        left join fetch pe.contratActuel
        where pe.employer.id = :idEmployer
        order by pe.dateDebut desc
        """)
    List<PeriodeEmploi> findByEmployerIdWithContratActuel(@Param("idEmployer") int idEmployer);
}