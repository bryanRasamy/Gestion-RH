package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DemandeService {
    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DetailDemandeRepository detailDemandeRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private HistoriqueDemandeStatusRepository historiqueRepository;

    @Transactional
    public Demande save(Demande demande) throws Exception{

        // 1. Créer la demande
        demande.setMatricule(genererMatricule());
        
        Demande saved = demandeRepository.save(demande);

        // 2. Créer chaque ligne de détail
        List<DetailDemande> detailDemandes = demande.getdetailDemandes();

        for (DetailDemande detailDemande : detailDemandes) {
            detailDemande.setDemande(saved);
            
            detailDemandeRepository.save(detailDemande);
        }

        // 3. Historique : statut "En attente"
        Status status = statusRepository.findBySigle("CR");

        if (status == null) {
            throw new Exception("Status introuvable");
        }

        HistoriqueDemandeStatus hist = new HistoriqueDemandeStatus();
        hist.setDemande(saved);
        hist.setEmployer(saved.getEmployer());
        hist.setStatus(status);
        hist.setDate(LocalDate.now());

        historiqueRepository.save(hist);

        return saved;
    }

    private String genererMatricule() {
        long total = demandeRepository.count() + 1;
        String matricule;
        do {
            matricule = String.format("DM%03d", total);
            total++;
        } while (demandeRepository.existsByMatricule(matricule));
        return matricule;
    }
}