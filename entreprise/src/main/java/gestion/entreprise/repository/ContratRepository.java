package gestion.entreprise.repository;

import gestion.entreprise.modele.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContratRepository extends JpaRepository<Contrat, Integer> {

    List<Contrat> findByStatutId(int idStatut);

    /** Le contrat d'embauche initial (premier contrat lié à la proposition). */
    Optional<Contrat> findFirstByPropositionIdOrderByIdAsc(int idProposition);

    /** Compatibilité. */
    default Contrat findFirstByPropositionId(int idProposition) {
        return findFirstByPropositionIdOrderByIdAsc(idProposition).orElse(null);
    }

    /**
     * Historique complet des contrats d'une période d'emploi, du plus ancien au plus récent.
     * On passe par la table affectation qui porte le lien période → contrat.
     */
    @Query("""
        select c from Contrat c
        join Affectation a on a.contrat.id = c.id
        where a.periodeEmploi.id = :idPeriode
        order by c.dateDebut asc, c.id asc
        """)
    List<Contrat> findHistoriqueByPeriodeEmploi(@Param("idPeriode") int idPeriode);

    /**
     * Tous les contrats d'un employé (toutes périodes confondues), du plus récent au plus ancien.
     */
    @Query("""
        select c from Contrat c
        join Affectation a on a.contrat.id = c.id
        join a.periodeEmploi pe
        where pe.employer.id = :idEmployer
        order by c.dateDebut desc, c.id desc
        """)
    List<Contrat> findAllByEmployerOrderByDateDebutDesc(@Param("idEmployer") int idEmployer);
}