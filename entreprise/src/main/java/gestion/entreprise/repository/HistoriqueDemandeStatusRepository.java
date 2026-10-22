package gestion.entreprise.repository;

import gestion.entreprise.modele.HistoriqueDemandeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Historique des actions realisees sur une demande. */
public interface HistoriqueDemandeStatusRepository extends JpaRepository<HistoriqueDemandeStatus, Integer> {

    /** Dernier statut connu d'une demande (statut courant). */
    HistoriqueDemandeStatus findTopByIdDemandeOrderByDateDescIdDesc(int idDemande);

    /** Historique complet d'une demande, du plus ancien au plus recent. */
    List<HistoriqueDemandeStatus> findByIdDemandeOrderByDateAsc(int idDemande);

    /** Toutes les actions realisees pour un statut donne. */
    List<HistoriqueDemandeStatus> findByStatus_LibelleIgnoreCase(String libelle);
}
