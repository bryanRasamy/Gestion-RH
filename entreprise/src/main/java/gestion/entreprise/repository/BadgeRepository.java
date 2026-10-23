package gestion.entreprise.repository;

import gestion.entreprise.modele.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadgeRepository extends JpaRepository<Badge, Integer> {
    Badge findFirstByEmployerIdOrderByIdDesc(int idEmployer);
    boolean existsByNumero(String numero);
}
