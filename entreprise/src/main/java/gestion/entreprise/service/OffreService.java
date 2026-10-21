package gestion.entreprise.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

@Service
public class OffreService {

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    // Offres des demandes validées dont la date limite n'est pas dépassée
    public List<DetailDemande> getOffresActives() {
        return detailDemandeRepository.findOffresActives(LocalDate.now());
    }

    public DetailDemande getOffreActive(int idDetail) throws Exception {
        DetailDemande offre = detailDemandeRepository.findOffreActiveById(idDetail, LocalDate.now());

        if (offre == null) {
            throw new Exception("Cette offre n'existe pas ou n'est plus active");
        }

        return offre;
    }
}
