package gestion.entreprise.repository;

import gestion.entreprise.modele.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
    Utilisateur findByEmail(String email);
    Utilisateur findByEmployer_Id(int idEmployer);
}
