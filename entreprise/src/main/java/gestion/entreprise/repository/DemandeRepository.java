package gestion.entreprise.repository;

import gestion.entreprise.modele.Demande;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    Demande findFirstById(int id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Demande d WHERE d.id = :id")
    Demande verrouillerDemande(@Param("id") int id);
    List<Demande> findAllByOrderByIdDesc();
    List<Demande> findAllByEmployer_IdOrderByIdDesc(int idEmployer);
}
