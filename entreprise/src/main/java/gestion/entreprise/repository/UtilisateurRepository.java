package gestion.entreprise.repository;

import gestion.entreprise.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Utilisateur findByEmail(String email);
    Utilisateur findByEmployer_Id(int idEmployer);
    List<Utilisateur> findByIdRole(int idRole);
}
