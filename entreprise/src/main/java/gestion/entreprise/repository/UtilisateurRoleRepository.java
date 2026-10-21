package gestion.entreprise.repository;

import gestion.entreprise.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRoleRepository extends JpaRepository<Utilisateur, Integer> {
    Utilisateur findFirstById(int id);
}
