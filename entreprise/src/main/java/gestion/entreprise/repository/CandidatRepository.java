package gestion.entreprise.repository;

import gestion.entreprise.modele.Candidat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

    List<Candidat> findByUtilisateurId(int idUtilisateur);


    // Dernière candidature d'un utilisateur (pour pré-remplir le formulaire)
    Candidat findFirstByUtilisateurIdOrderByIdDesc(int idUtilisateur);

    boolean existsByUtilisateurIdAndDetailDemandeId(int idUtilisateur, int idDetailDemande);
}
