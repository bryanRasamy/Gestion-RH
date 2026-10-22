package gestion.entreprise.repository;

import gestion.entreprise.modele.DetailDemande;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetailDemandeRepository extends JpaRepository<DetailDemande, Integer> {

    // Offres = détails des demandes dont le DERNIER statut est "Valider" (sigle AU)
    // et dont la date limite n'est pas encore dépassée.
    // La version JPQL charge aussi les relations nécessaires au rendu Thymeleaf.
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
                    and h.status.sigle = 'AU'
              )
            order by d.dateLimite, dd.id
            """)
    List<DetailDemande> findOffresActives(@Param("aujourdhui") LocalDate aujourdhui);

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
                    and h.status.sigle = 'AU'
              )
            """)
    DetailDemande findOffreActiveById(@Param("idDetail") int idDetail, @Param("aujourdhui") LocalDate aujourdhui);
}
