package gestion.entreprise.service;

import gestion.entreprise.dto.DemandeListeItem;
import gestion.entreprise.dto.EcranCorrection;
import gestion.entreprise.dto.LigneComparaison;
import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DemandeARevoir;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.Employer;
import gestion.entreprise.modele.HistoriqueDemandeStatus;
import gestion.entreprise.modele.Poste;
import gestion.entreprise.modele.Role;
import gestion.entreprise.modele.StatutDemande;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.DemandeARevoirRepository;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.DepartementRepository;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.EmployerRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import gestion.entreprise.repository.PosteRepository;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Ecran de correction du manager : il compare sa demande d'origine avec la
 * revision demandee par le RH, la Finance ou la DG, puis accepte ou abandonne.
 */
@Service
public class CorrectionService {

    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailRepository;
    private final DemandeARevoirRepository revoirRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final EmployerRepository employerRepository;
    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final ValidationService validationService;

    public CorrectionService(DemandeRepository demandeRepository,
                             DetailDemandeRepository detailRepository,
                             DemandeARevoirRepository revoirRepository,
                             HistoriqueDemandeStatusRepository historiqueRepository,
                             EmployerRepository employerRepository,
                             PosteRepository posteRepository,
                             DepartementRepository departementRepository,
                             UtilisateurRepository utilisateurRepository,
                             RoleRepository roleRepository,
                             ValidationService validationService) {
        this.demandeRepository = demandeRepository;
        this.detailRepository = detailRepository;
        this.revoirRepository = revoirRepository;
        this.historiqueRepository = historiqueRepository;
        this.employerRepository = employerRepository;
        this.posteRepository = posteRepository;
        this.departementRepository = departementRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.validationService = validationService;
    }

    /** Demandes du manager actuellement en attente de correction. */
    @Transactional(readOnly = true)
    public List<DemandeListeItem> listerDemandesARevoir(int idEmploye) {
        List<DemandeListeItem> resultat = new ArrayList<>();
        for (Demande demande : demandeRepository.findAll()) {
            if (demande.getIdEmployer() != idEmploye) {
                continue;
            }
            StatutDemande courant = validationService.statutCourant(demande.getId());
            if (courant != StatutDemande.A_REVOIR) {
                continue;
            }
            HistoriqueDemandeStatus dernier = historiqueRepository
                    .findTopByIdDemandeOrderByDateDescIdDesc(demande.getId());
            resultat.add(new DemandeListeItem(
                    demande.getId(), demande.getMatricule(), demande.getIdEmployer(),
                    demande.getDateLimite(), courant.getLibelle(),
                    dernier != null ? dernier.getDate() : null,
                    dernier != null ? dernier.getMotif() : null,
                    null, null, null, null,
                    detailRepository.findByIdDemande(demande.getId()).size()));
        }
        return resultat;
    }

    /** Prepare l'ecran deux colonnes (demande d'origine / revision). */
    @Transactional(readOnly = true)
    public EcranCorrection ecran(int idDemande, int idEmploye) {
        Demande demande = demandeRepository.findOneById(idDemande);
        if (demande == null) {
            throw new IllegalArgumentException("Demande introuvable.");
        }
        if (demande.getIdEmployer() != idEmploye) {
            throw new IllegalStateException("Cette demande ne vous appartient pas.");
        }
        if (validationService.statutCourant(idDemande) != StatutDemande.A_REVOIR) {
            throw new IllegalStateException("La demande n'est plus en attente de correction.");
        }

        HistoriqueDemandeStatus revision = derniereRevision(idDemande);
        List<DemandeARevoir> retours = revoirRepository.findByIdDemandeDetailsIn(
                idsDetails(idDemande));

        List<LigneComparaison> lignes = new ArrayList<>();
        for (DetailDemande detail : detailRepository.findByIdDemande(idDemande)) {
            DemandeARevoir retour = null;
            for (DemandeARevoir candidat : retours) {
                if (candidat.getIdDemandeDetails() == detail.getId()) {
                    retour = candidat;
                    break;
                }
            }
            if (retour == null) {
                continue; // ligne non concernee par la revision
            }
            Employer auteur = employerRepository.findOneById(retour.getIdDemandeur());
            lignes.add(new LigneComparaison(
                    detail.getId(),
                    libellePoste(detail.getIdPoste()),
                    libelleDepartement(detail.getIdDepartement()),
                    detail.getDescription(),
                    detail.getNbrPersonnel(),
                    retour.getNbrPersonnelPropose(),
                    retour.getCommentaire(),
                    auteur != null ? auteur.getPrenom() + " " + auteur.getNom() : "Inconnu",
                    libelleRole(retour.getIdDemandeur()),
                    retour.getDate()));
        }

        Employer auteur = revision != null && revision.getIdEmploye() != null
                ? employerRepository.findOneById(revision.getIdEmploye()) : null;

        return new EcranCorrection(
                demande.getId(), demande.getMatricule(), demande.getDateLimite(),
                StatutDemande.A_REVOIR.getLibelle(),
                auteur != null ? auteur.getPrenom() + " " + auteur.getNom() : "Inconnu",
                libelleRole(revision != null && revision.getIdEmploye() != null
                        ? revision.getIdEmploye() : 0),
                revision != null ? revision.getDate() : null,
                revision != null ? revision.getMotif() : null,
                lignes);
    }

    /** Le manager accepte la revision : la demande reprend le circuit a l'etage demandeur. */
    public void accepter(int idDemande, int idEmploye) {
        validationService.accepterRevision(idDemande, idEmploye);
    }

    /** Le manager abandonne sa demande de revision. */
    public void abandonner(int idDemande, int idEmploye) {
        validationService.abandonnerRevision(idDemande, idEmploye);
    }

    /* ---------------------------------------------------------------- */
    // Prives
    /* ---------------------------------------------------------------- */

    /** Derniere ligne d'historique 'a revoir' de la demande. */
    private HistoriqueDemandeStatus derniereRevision(int idDemande) {
        List<HistoriqueDemandeStatus> historique = historiqueRepository
                .findByIdDemandeOrderByDateAsc(idDemande);
        for (int i = historique.size() - 1; i >= 0; i--) {
            HistoriqueDemandeStatus ligne = historique.get(i);
            if (ligne.getStatus() != null
                    && StatutDemande.A_REVOIR.getLibelle().equalsIgnoreCase(ligne.getStatus().getLibelle())) {
                return ligne;
            }
        }
        return null;
    }

    private List<Integer> idsDetails(int idDemande) {
        List<Integer> ids = new ArrayList<>();
        for (DetailDemande detail : detailRepository.findByIdDemande(idDemande)) {
            ids.add(detail.getId());
        }
        return ids;
    }

    private String libellePoste(int idPoste) {
        Poste poste = posteRepository.findOneById(idPoste);
        return poste != null ? poste.getLibelle() : "Poste #" + idPoste;
    }

    private String libelleDepartement(int idDepartement) {
        var departement = departementRepository.findOneById(idDepartement);
        return departement != null ? departement.getLibelle() : "Departement #" + idDepartement;
    }

    /** Role applicatif de l'employe auteur de la revision. */
    private String libelleRole(int idEmploye) {
        if (idEmploye <= 0) {
            return "Inconnu";
        }
        Utilisateur utilisateur = utilisateurRepository.findByEmployer_Id(idEmploye);
        if (utilisateur == null || utilisateur.getIdRole() <= 0) {
            return "Inconnu";
        }
        Role role = roleRepository.findOneById(utilisateur.getIdRole());
        return role != null ? role.getLibelle() : "Inconnu";
    }
}
