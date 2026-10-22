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
                                 int idDetail, String commentaire, int nombrePropose,
                                 boolean annulerPoste) {
        Demande demande = demandeRepository.findFirstById(idDemande);
        if (demande == null) {
            throw new RuntimeException("Demande introuvable");
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

        if (commentaire == null || commentaire.trim().isEmpty()) {
            throw new RuntimeException("Un commentaire est requis pour la ligne à revoir");
        }
        if (nombrePropose < 0) {
            throw new RuntimeException("Le nombre proposé ne peut pas être négatif");
        }
        if (annulerPoste) {
            nombrePropose = 0;
        }

        DetailDemande detail = detailRepository.findFirstById(idDetail);
        if (detail == null || detail.getDemande().getId() != idDemande) {
            throw new RuntimeException("La ligne de révision ne correspond pas à la demande");
        }

        DemandeARevoir revision = new DemandeARevoir();
        revision.setMpangataka(employerRepository.getReferenceById(idEmployer));
        revision.setDetailDemande(detail);
        revision.setEtapeDemandeuse(etapeDemandeuse);
        revision.setDate(LocalDate.now());
        revision.setCommentaire(commentaire);
        revision.setNbrPersonnelPropose(nombrePropose);
        revision.setAnnulerPoste(annulerPoste);
        revision.setTraite(false);
        revisionRepository.save(revision);

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

    public List<DemandeARevoir> correctionsToutes() {
        return revisionRepository.findAllByTraiteFalse();
    }

    public List<DemandeARevoir> correctionsDemande(int idDemande, int idEmployer,
                                                   boolean superAdmin) {
        Demande demande = demandeRepository.findFirstById(idDemande);
        if (demande == null || (!superAdmin && demande.getEmployer().getId() != idEmployer)) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        if (!"à revoir".equalsIgnoreCase(statutService.statutCourant(idDemande))) {
            throw new RuntimeException("Cette demande n'est pas en attente de correction");
        }
        return revisionRepository.findAllByDetailDemande_Demande_IdAndTraiteFalse(idDemande);
    }

    @Transactional
    public void accepterCorrection(int idDemande, int idEmployer, boolean superAdmin) {
        Demande demande = demandeRepository.getReferenceById(idDemande);
        if (!superAdmin && demande.getEmployer().getId() != idEmployer) {
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
            if (revision.isAnnulerPoste()) {
                revision.getDetailDemande().setNbrPersonnel(0);
                detailRepository.save(revision.getDetailDemande());
            } else if (revision.getNbrPersonnelPropose() > 0) {
                revision.getDetailDemande().setNbrPersonnel(revision.getNbrPersonnelPropose());
                detailRepository.save(revision.getDetailDemande());
            }
            revision.setTraite(true);
            revisionRepository.save(revision);
        }
        statutService.changer(idDemande, "validé " + etape, idEmployer, null);
    }

    @Transactional
    public void abandonner(int idDemande, int idEmployer, boolean superAdmin) {
        Demande demande = demandeRepository.getReferenceById(idDemande);
        if (!superAdmin && demande.getEmployer().getId() != idEmployer) {
            throw new RuntimeException("Cette demande ne vous appartient pas");
        }
        statutService.changer(idDemande, "abandonné", idEmployer, null);
    }
}
