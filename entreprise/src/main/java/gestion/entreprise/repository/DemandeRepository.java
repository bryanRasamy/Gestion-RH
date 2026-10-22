package gestion.entreprise.repository;

import gestion.entreprise.modele.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    boolean existsByMatricule(String matricule);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Demande d where d.id = :id")
    Demande findByIdForUpdate(@Param("id") int id);
}
