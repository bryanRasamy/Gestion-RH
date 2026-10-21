package gestion.entreprise.repository;

import gestion.entreprise.modele.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findOneById(int id);
    Role findByLibelleIgnoreCase(String libelle);
}