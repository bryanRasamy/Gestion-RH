package gestion.entreprise.repository;

import gestion.entreprise.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Utilisateur findByEmail(String email);

    /**
     * Retourne tous les utilisateurs ayant un rôle donné (par sigle du rôle).
     * Utilisé pour retrouver les id_employer des évaluateurs d'un rôle.
     */
    @Query(value = "SELECT u.* FROM utilisateur u JOIN role r ON u.id_role = r.id WHERE r.sigle = :sigleRole AND u.id_employer IS NOT NULL", nativeQuery = true)
    List<Utilisateur> findByRoleSigle(@Param("sigleRole") String sigleRole);
}
