package gestion.entreprise.repository;

import gestion.entreprise.modele.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PosteRepository extends JpaRepository<Poste, Integer> {

}
