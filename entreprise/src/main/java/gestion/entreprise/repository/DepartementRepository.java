package gestion.entreprise.repository;

import gestion.entreprise.modele.Departement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartementRepository extends JpaRepository<Departement, Integer> {
    Departement findOneById(int id);
}