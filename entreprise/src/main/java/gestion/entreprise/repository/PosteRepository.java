package gestion.entreprise.repository;

import gestion.entreprise.modele.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosteRepository extends JpaRepository<Poste, Integer> {
    List<Poste> findByDepartement_Id(int idDepartement);
}