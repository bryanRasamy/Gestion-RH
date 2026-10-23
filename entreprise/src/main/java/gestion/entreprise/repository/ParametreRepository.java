package gestion.entreprise.repository;

import gestion.entreprise.modele.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    Parametre findByCode(String code);
}