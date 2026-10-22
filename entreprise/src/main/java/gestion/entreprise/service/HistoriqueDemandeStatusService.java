package gestion.entreprise.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gestion.entreprise.repository.*;
import gestion.entreprise.vue.*;

@Service
public class HistoriqueDemandeStatusService {

    @Autowired 
    private HistoriqueDemandeStatusViewRepository historiqueDemandeStatusViewRepository;

    public List<HistoriqueDemandeStatusView> getDemandesRecent(){
        return historiqueDemandeStatusViewRepository.findLatestStatusPerDemand();
    }
}