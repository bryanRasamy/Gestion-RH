package gestion.entreprise.repository;

import gestion.entreprise.modele.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployerRepository extends JpaRepository<Employer, Integer> {
    Employer findOneById(int id);
}
