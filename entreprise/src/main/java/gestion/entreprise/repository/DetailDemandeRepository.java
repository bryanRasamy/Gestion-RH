package gestion.entreprise.repository;

import gestion.entreprise.modele.DetailDemande;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {

    /**
     * Tous les détails d'une demande donnée.
     * Utilisé par DemandeController et HistoriqueDemandeStatusService.
     */
    List<DetailDemande> findByDemandeId(int idDemande);

    /**
     * Frontoffice — liste des offres actives :
     * détails de demandes dont le DERNIER statut est "Valider" (VA)
     * et dont la date limite n'est pas encore dépassée.
     *
     * On utilise le MAX(id) pour identifier la dernière ligne d'historique
     * (robuste aux changements d'horloge, cohérent avec le backoffice).
     */
    @Query("""
            select distinct dd
            from DetailDemande dd
            join fetch dd.demande d
            join fetch dd.poste p
            join fetch p.departement dep
            where d.dateLimite >= :aujourdhui
              and exists (
                  select 1
                  from HistoriqueDemandeStatus h
                  where h.demande = d
                    and h.id = (
                        select max(h2.id)
                        from HistoriqueDemandeStatus h2
                        where h2.demande = d
                    )
                    and h.status.sigle = 'VA'
              )
            order by d.dateLimite, dd.id
            """)
    List<DetailDemande> findOffresActives(@Param("aujourdhui") LocalDateTime aujourdhui);

    /**
     * Frontoffice — détail d'une offre active.
     */
    @Query("""
            select distinct dd
            from DetailDemande dd
            join fetch dd.demande d
            join fetch dd.poste p
            join fetch p.departement dep
            where dd.id = :idDetail
              and d.dateLimite >= :aujourdhui
              and exists (
                  select 1
                  from HistoriqueDemandeStatus h
                  where h.demande = d
                    and h.id = (
                        select max(h2.id)
                        from HistoriqueDemandeStatus h2
                        where h2.demande = d
                    )
                    and h.status.sigle = 'VA'
              )
            """)
    DetailDemande findOffreActiveById(@Param("idDetail") int idDetail,
                                      @Param("aujourdhui") LocalDateTime aujourdhui);
}