package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeARevoirService {

    @Autowired
    private DemandeARevoirRepository revisionRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private DemandeStatutService statutService;

    @Autowired
    private DetailDemandeRepository detailRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Transactional
    public void demanderRevision(int idDemande, int idEmployer, String etapeDemandeuse,
                                 int[] details, String[] commentaires, int[] nombres) {
        Demande demande = demandeRepository.getReferenceById(idDemande);
        if (demande.getEmployer().getId() != idEmployer) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        if (details == null || commentaires == null || nombres == null || details.length == 0
            || details.length != commentaires.length || details.length != nombres.length) {
            throw new RuntimeException("Les détails de la révision sont incomplets");
        }
        String statut = statutService.statutCourant(idDemande);
        if (!"créé".equalsIgnoreCase(statut) && !"validé RH".equalsIgnoreCase(statut)
            && !"validé Finance".equalsIgnoreCase(statut)) {
            throw new RuntimeException("La demande ne peut pas être envoyée en révision à cette étape");
        }

        String etapeAttendue = "créé".equalsIgnoreCase(statut) ? "RH"
            : "validé RH".equalsIgnoreCase(statut) ? "Finance" : "DG";
        if (!etapeAttendue.equalsIgnoreCase(etapeDemandeuse)) {
            throw new RuntimeException("Le rôle ne correspond pas à l'étape actuelle");
        }

        for (int index = 0; index < details.length; index++) {
            String commentaire = commentaires[index];
            if (commentaire == null || commentaire.trim().isEmpty()) {
                throw new RuntimeException("Un commentaire est requis pour chaque ligne à revoir");
            }
            DemandeARevoir revision = new DemandeARevoir();
            revision.setMpangataka(employerRepository.getReferenceById(idEmployer));
            DetailDemande detail = detailRepository.findFirstById(details[index]);
            if (detail == null || detail.getDemande().getId() != idDemande) {
                throw new RuntimeException("Une ligne de révision ne correspond pas à la demande");
            }
            revision.setDetailDemande(detail);
            revision.setEtapeDemandeuse(etapeDemandeuse);
            revision.setDate(LocalDate.now());
            revision.setCommentaire(commentaire);
            revision.setNbrPersonnelPropose(nombres[index]);
            revision.setTraite(false);
            revisionRepository.save(revision);
        }
        statutService.changer(idDemande, "à revoir", idEmployer, null);
    }

    public List<DemandeARevoir> correctionsManager(int idEmployer) {
        List<Demande> demandes = demandeRepository.findAllByEmployer_IdOrderByIdDesc(idEmployer);
        java.util.ArrayList<DemandeARevoir> resultats = new java.util.ArrayList<>();
        for (Demande demande : demandes) {
            if ("à revoir".equalsIgnoreCase(statutService.statutCourant(demande.getId()))) {
                resultats.addAll(revisionRepository.findAllByDetailDemande_Demande_IdAndTraiteFalse(demande.getId()));
            }
        }
        return resultats;
    }

    public List<DemandeARevoir> correctionsDemande(int idDemande, int idEmployer) {
        Demande demande = demandeRepository.findFirstById(idDemande);
        if (demande == null || demande.getEmployer().getId() != idEmployer) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        if (!"à revoir".equalsIgnoreCase(statutService.statutCourant(idDemande))) {
            throw new RuntimeException("Cette demande n'est pas en attente de correction");
        }
        return revisionRepository.findAllByDetailDemande_Demande_IdAndTraiteFalse(idDemande);
    }

    @Transactional
    public void accepterCorrection(int idDemande, int idEmployer) {
        Demande demande = demandeRepository.getReferenceById(idDemande);
        if (demande.getEmployer().getId() != idEmployer) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        List<DemandeARevoir> revisions = revisionRepository.findAllByDetailDemande_Demande_IdAndTraiteFalse(idDemande);
        if (revisions.isEmpty()) {
            throw new RuntimeException("Aucune correction en attente pour cette demande");
        }

        String etape = revisions.get(0).getEtapeDemandeuse();
        for (DemandeARevoir revision : revisions) {
            if (!etape.equalsIgnoreCase(revision.getEtapeDemandeuse())) {
                throw new RuntimeException("Les corrections de plusieurs étapes doivent être traitées séparément");
            }
            if (revision.getNbrPersonnelPropose() > 0) {
                revision.getDetailDemande().setNbrPersonnel(revision.getNbrPersonnelPropose());
                detailRepository.save(revision.getDetailDemande());
            }
            revision.setTraite(true);
            revisionRepository.save(revision);
        }
        statutService.changer(idDemande, "validé " + etape, idEmployer, null);
    }

    @Transactional
    public void abandonner(int idDemande, int idEmployer) {
        Demande demande = demandeRepository.getReferenceById(idDemande);
        if (demande.getEmployer().getId() != idEmployer) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        statutService.changer(idDemande, "abandonné", idEmployer, null);
    }
}
