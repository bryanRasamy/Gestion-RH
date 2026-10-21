package gestion.entreprise.repository;

import gestion.entreprise.modele.DetailDemande;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {

    // Offres = détails des demandes dont le DERNIER statut est "Valider" (sigle AU)
    // et dont la date limite n'est pas encore dépassée
    String OFFRES_ACTIVES = "SELECT dd.* FROM detail_demande dd "
            + "JOIN demande d ON d.id = dd.id_demande "
            + "WHERE d.date_limite >= :aujourdhui "
            + "AND (SELECT s.sigle FROM historique_demande_status h "
            + "     JOIN status s ON s.id = h.id_status "
            + "     WHERE h.id = (SELECT MAX(h2.id) FROM historique_demande_status h2 "
            + "                   WHERE h2.id_demande = d.id)) = 'AU' ";

    @Query(value = OFFRES_ACTIVES + "ORDER BY d.date_limite, dd.id", nativeQuery = true)
    List<DetailDemande> findOffresActives(@Param("aujourdhui") LocalDate aujourdhui);

    @Query(value = OFFRES_ACTIVES + "AND dd.id = :idDetail", nativeQuery = true)
    DetailDemande findOffreActiveById(@Param("idDetail") int idDetail, @Param("aujourdhui") LocalDate aujourdhui);
}
