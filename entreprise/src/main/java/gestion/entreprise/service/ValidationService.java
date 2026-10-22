package gestion.entreprise.service;

import gestion.entreprise.dto.DemandeCarte;
import gestion.entreprise.dto.DemandeListeItem;
import gestion.entreprise.dto.EvaluationBudget;
import gestion.entreprise.dto.LigneDemande;
import gestion.entreprise.dto.MontantDemande;
import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DemandeARevoir;
import gestion.entreprise.modele.Departement;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.Diplome;
import gestion.entreprise.modele.Employer;
import gestion.entreprise.modele.HistoriqueDemandeStatus;
import gestion.entreprise.modele.Poste;
import gestion.entreprise.modele.Role;
import gestion.entreprise.modele.StatutDemande;
import gestion.entreprise.modele.Status;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.DemandeARevoirRepository;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.DepartementRepository;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.DiplomeRepository;
import gestion.entreprise.repository.EmployerRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import gestion.entreprise.repository.PosteRepository;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.repository.StatusRepository;
import gestion.entreprise.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Circuit de validation d'une demande de recrutement.
 *
 * <p>Enchainement : le manager cree la demande ({@code cree}), la RH la
 * valide ({@code valider rh}), la Finance ({@code valider finance}), puis
 * la DG ({@code valider dg}). A chaque etape la demande peut etre renvoyee au
 * manager ({@code a revoir}) ou refusee ({@code refuser}).</p>
 *
 * <p>Toute ecriture dans {@code historique_demande_status} passe par ce
 * service : les controles de transition, de role et d'expiration y sont
 * appliques cote serveur.</p>
 */
@Service
public class ValidationService {

    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final DemandeARevoirRepository revoirRepository;
    private final StatusRepository statusRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PosteRepository posteRepository;
    private final DepartementRepository departementRepository;
    private final DiplomeRepository diplomeRepository;
    private final EmployerRepository employerRepository;
    private final BudgetService budgetService;

    public ValidationService(DemandeRepository demandeRepository,
                             DetailDemandeRepository detailRepository,
                             HistoriqueDemandeStatusRepository historiqueRepository,
                             DemandeARevoirRepository revoirRepository,
                             StatusRepository statusRepository,
                             UtilisateurRepository utilisateurRepository,
                             RoleRepository roleRepository,
                             PosteRepository posteRepository,
                             DepartementRepository departementRepository,
                             DiplomeRepository diplomeRepository,
                             EmployerRepository employerRepository,
                             BudgetService budgetService) {
        this.demandeRepository = demandeRepository;
        this.detailRepository = detailRepository;
        this.historiqueRepository = historiqueRepository;
        this.revoirRepository = revoirRepository;
        this.statusRepository = statusRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.posteRepository = posteRepository;
        this.departementRepository = departementRepository;
        this.diplomeRepository = diplomeRepository;
        this.employerRepository = employerRepository;
        this.budgetService = budgetService;
    }

    /* ------------------------------------------------------------------ */
    // Lecture
    /* ------------------------------------------------------------------ */

    /** Statut courant d'une demande : derniere ligne de l'historique. */
    @Transactional(readOnly = true)
    public StatutDemande statutCourant(int idDemande) {
        HistoriqueDemandeStatus dernier = historiqueRepository
                .findTopByIdDemandeOrderByDateDescIdDesc(idDemande);
        if (dernier == null || dernier.getStatus() == null) {
            return null;
        }
        return StatutDemande.depuisLibelle(dernier.getStatus().getLibelle());
    }

    /**
     * Demandes relevant de l'etage d'un role, filtrees par date et par statut.
     * L'expiration est appliquee avant le filtrage.
     */
    @Transactional
    public List<DemandeListeItem> lister(int idRole, LocalDate dateMin, LocalDate dateMax,
                                         String statutFiltre) {
        expirerDemandesDepassees();

        TypeRole role = roleDeRole(idRole);
        if (role == null) {
            return List.of();
        }
        StatutDemande attendu = StatutDemande.statutAttendu(role);

        List<DemandeListeItem> resultat = new ArrayList<>();
        for (Demande demande : demandeRepository.findAll()) {
            StatutDemande courant = statutCourant(demande.getId());
            if (courant == null || courant.estFinal()) {
                continue;
            }
            if (courant != attendu) {
                continue;
            }
            if (role == TypeRole.MANAGER && !estDemandeur(demande, idRole)) {
                continue;
            }

            HistoriqueDemandeStatus dernier = historiqueRepository
                    .findTopByIdDemandeOrderByDateDescIdDesc(demande.getId());
            LocalDate statutDate = dernier != null ? dernier.getDate() : null;
            String motif = dernier != null ? dernier.getMotif() : null;

            if (!dansPeriode(statutDate, dateMin, dateMax)) {
                continue;
            }
            if (statutFiltre != null && !statutFiltre.isBlank()
                    && !statutFiltre.equalsIgnoreCase(courant.getLibelle())) {
                continue;
            }

            MontantDemande montant = budgetService.montantDemande(demande.getId());
            EvaluationBudget evaluation = role == TypeRole.FINANCE
                    ? budgetService.evaluer(demande.getId()) : null;

            resultat.add(new DemandeListeItem(
                    demande.getId(), demande.getMatricule(), demande.getIdEmployer(),
                    demande.getDateLimite(), courant.getLibelle(), statutDate, motif,
                    montant.min(), montant.max(), budgetService.budgetCourant(),
                    evaluation != null ? evaluation.getLibelle() : null,
                    detailRepository.findByIdDemande(demande.getId()).size()));
        }
        return resultat;
    }

    /**
     * Demandes de l'etage du role connecte, completees pour l'affichage
     * (lignes de detail, demandeur, montants).
     */
    @Transactional
    public List<DemandeCarte> listerCartes(int idRole, LocalDate dateMin, LocalDate dateMax,
                                            String statutFiltre) {
        List<DemandeCarte> cartes = new ArrayList<>();
        for (DemandeListeItem item : lister(idRole, dateMin, dateMax, statutFiltre)) {
            cartes.add(carte(item));
        }
        return cartes;
    }

    /** Complete une ligne de liste avec les informations d'affichage. */
    public DemandeCarte carte(DemandeListeItem item) {
        Demande demande = demandeRepository.findOneById(item.getId());
        Employer demandeur = demande != null ? employerRepository.findOneById(demande.getIdEmployer()) : null;
        return new DemandeCarte(
                item.getId(), item.getMatricule(), item.getIdEmployer(),
                demandeur != null ? demandeur.getPrenom() + " " + demandeur.getNom() : "",
                item.getDateLimite(), item.getStatut(), item.getStatutDate(), item.getStatutMotif(),
                item.getMontantMin(), item.getMontantMax(), item.getBudget(), item.getEvaluation(),
                lignes(item.getId()));
    }

    /** Cartes completes d'une liste de demandes. */
    public List<DemandeCarte> cartes(List<DemandeListeItem> items) {
        List<DemandeCarte> cartes = new ArrayList<>();
        for (DemandeListeItem item : items) {
            cartes.add(carte(item));
        }
        return cartes;
    }

    /** Lignes de detail d'une demande, enrichies des libelles de reference. */
    public List<LigneDemande> lignes(int idDemande) {
        List<LigneDemande> lignes = new ArrayList<>();
        for (DetailDemande detail : detailRepository.findByIdDemande(idDemande)) {
            Poste poste = posteRepository.findOneById(detail.getIdPoste());
            Departement departement = departementRepository.findOneById(detail.getIdDepartement());
            Diplome diplome = diplomeRepository.findOneById(detail.getIdDiplome());
            lignes.add(new LigneDemande(
                    detail.getId(),
                    poste != null ? poste.getLibelle() : "Poste #" + detail.getIdPoste(),
                    departement != null ? departement.getLibelle()
                            : "Departement #" + detail.getIdDepartement(),
                    detail.getDescription(),
                    detail.getNbrPersonnel(),
                    diplome != null ? diplome.getNomDiplome() : "",
                    detail.getAnneeExperience()));
        }
        return lignes;
    }

    /* ------------------------------------------------------------------ */
    // Actions
    /* ------------------------------------------------------------------ */
    /**
     * Change le statut d'une demande.
     *
     * @param idDemande demande concernee
     * @param nouveau   statut cible
     * @param idEmploye employe qui realise l'action (colonne {@code id_employer})
     * @param motif     raison du refus ou commentaire (obligatoire pour un refus)
     */
    @Transactional
    public void changer(int idDemande, StatutDemande nouveau, int idEmploye, String motif) {
        Demande demande = chargerDemande(idDemande);
        StatutDemande courant = statutCourant(idDemande);

        if (courant == null) {
            throw new IllegalStateException("La demande n'a pas de statut courant.");
        }
        if (courant.estFinal()) {
            throw new IllegalStateException("La demande est deja cloturee.");
        }
        if (!courant.transitionsAutorisees().contains(nouveau)) {
            throw new IllegalStateException("Transition interdite : " + courant.getLibelle()
                    + " -> " + nouveau.getLibelle());
        }
        if (!estDansDelai(demande)) {
            throw new IllegalStateException("La demande est expiree et ne peut plus etre traitee.");
        }

        TypeRole roleActeur = roleDeEmploye(idEmploye);
        if (roleActeur == null) {
            throw new IllegalStateException("L'utilisateur n'a pas de role affecte.");
        }
        if (!peutAgir(roleActeur, courant, idEmploye, demande)) {
            throw new IllegalStateException("Vous n'etes pas l'interlocuteur attitree de cette demande.");
        }
        if (nouveau == StatutDemande.REFUSER && (motif == null || motif.isBlank())) {
            throw new IllegalArgumentException("Le motif du refus est obligatoire.");
        }
        if (roleActeur == TypeRole.FINANCE && nouveau == StatutDemande.VALIDER_FINANCE
                && budgetService.evaluer(idDemande) == EvaluationBudget.NON) {
            throw new IllegalStateException("Le montant minimum depasse le budget annuel : "
                    + "cette demande ne peut pas etre validee par la Finance.");
        }

        enregistrer(idDemande, nouveau, idEmploye, motif);
    }

    /**
     * Demande une revision : la demande passe en {@code a revoir} et les
     * lignes concernees sont copiees dans {@code demande_a_revoir}.
     */
    @Transactional
    public void demanderRevision(int idDemande, int idEmploye, String commentaire,
                                 List<LigneRevision> lignes) {
        Demande demande = chargerDemande(idDemande);
        StatutDemande courant = statutCourant(idDemande);

        if (courant == null || courant.estFinal()) {
            throw new IllegalStateException("La demande ne peut pas etre remise a revoir.");
        }
        if (!estDansDelai(demande)) {
            throw new IllegalStateException("La demande est expiree et ne peut plus etre traitee.");
        }
        if (lignes == null || lignes.isEmpty()) {
            throw new IllegalArgumentException("Selectionnez au moins une ligne a corriger.");
        }

        TypeRole roleActeur = roleDeEmploye(idEmploye);
        if (roleActeur == null || !peutDemanderRevision(roleActeur, courant)) {
            throw new IllegalStateException("Votre role ne peut pas demander de revision a ce stade.");
        }

        enregistrer(idDemande, StatutDemande.A_REVOIR, idEmploye, commentaire);

        for (LigneRevision ligne : lignes) {
            DemandeARevoir retour = new DemandeARevoir();
            retour.setIdDemandeur(idEmploye);
            retour.setIdDemandeDetails(ligne.idDemandeDetails());
            retour.setDate(LocalDate.now());
            retour.setCommentaire(commentaire);
            retour.setNbrPersonnelPropose(ligne.nbrPersonnelPropose());
            revoirRepository.save(retour);
        }
    }

    /**
     * Le manager accepte la revision : les valeurs proposees sont appliquees
     * et la demande reprend le circuit a l'etage qui avait demande la revision.
     */
    @Transactional
    public void accepterRevision(int idDemande, int idEmploye) {
        Demande demande = chargerDemande(idDemande);
        if (demande.getIdEmployer() != idEmploye) {
            throw new IllegalStateException("Vous n'etes pas le demandeur de cette demande.");
        }
        if (statutCourant(idDemande) != StatutDemande.A_REVOIR) {
            throw new IllegalStateException("La demande n'est pas en attente de correction.");
        }

        StatutDemande cible = statutApresRevision(idDemande);
        List<DemandeARevoir> retours = revoirRepository
                .findByIdDemandeDetailsIn(idsDetails(idDemande));

        for (DemandeARevoir retour : retours) {
            if (retour.getNbrPersonnelPropose() == null) {
                continue;
            }
            DetailDemande detail = detailRepository.findOneById(retour.getIdDemandeDetails());
            if (detail != null) {
                detail.setNbrPersonnel(retour.getNbrPersonnelPropose());
                detailRepository.save(detail);
            }
        }

        enregistrer(idDemande, cible, idEmploye, "Correction acceptee par le manager");

        // Les lignes de revision sont consommees.
        for (DemandeARevoir retour : retours) {
            revoirRepository.delete(retour);
        }
    }

    /** Le manager abandonne : la demande passe en {@code abandone}. */
    @Transactional
    public void abandonnerRevision(int idDemande, int idEmploye) {
        Demande demande = chargerDemande(idDemande);
        if (demande.getIdEmployer() != idEmploye) {
            throw new IllegalStateException("Vous n'etes pas le demandeur de cette demande.");
        }
        if (statutCourant(idDemande) != StatutDemande.A_REVOIR) {
            throw new IllegalStateException("La demande n'est pas en attente de correction.");
        }

        enregistrer(idDemande, StatutDemande.ABANDONNE, idEmploye, "Abandonne par le manager");

        List<Integer> ids = idsDetails(idDemande);
        if (!ids.isEmpty()) {
            revoirRepository.deleteAllByIdDemandeDetailsIn(ids);
        }
    }

    /* ------------------------------------------------------------------ */
    // Expiration
    /* ------------------------------------------------------------------ */

    /** Passe en {@code expire} toute demande dont la date limite est depassee. */
    @Transactional
    public void expirerDemandesDepassees() {
        for (Demande demande : demandeRepository.findAll()) {
            if (!estDepassee(demande)) {
                continue;
            }
            StatutDemande courant = statutCourant(demande.getId());
            if (courant == null || courant.estFinal() || !courant.estExpirable()) {
                continue;
            }
            enregistrer(demande.getId(), StatutDemande.EXPIRE, null, "Date limite depassee");
        }
    }

    /* ------------------------------------------------------------------ */
    // Prives
    /* ------------------------------------------------------------------ */

    private Demande chargerDemande(int idDemande) {
        Demande demande = demandeRepository.findOneById(idDemande);
        if (demande == null) {
            throw new IllegalArgumentException("Demande introuvable.");
        }
        return demande;
    }

    /** Ecrit une ligne d'historique. {@code idEmploye} null = action systeme. */
    private void enregistrer(int idDemande, StatutDemande statut, Integer idEmploye, String motif) {
        Status cible = statusRepository.findByLibelleIgnoreCase(statut.getLibelle());
        if (cible == null) {
            throw new IllegalStateException("Le statut " + statut.getLibelle() + " est absent de la base.");
        }
        HistoriqueDemandeStatus ligne = new HistoriqueDemandeStatus();
        ligne.setIdDemande(idDemande);
        ligne.setStatus(cible);
        ligne.setIdEmploye(idEmploye);
        ligne.setDate(LocalDate.now());
        ligne.setMotif(motif);
        historiqueRepository.save(ligne);
    }

    /** Vrai tant que la date limite de la demande n'est pas depassee. */
    private boolean estDansDelai(Demande demande) {
        return !estDepassee(demande);
    }

    /** Vrai si la date limite de la demande est passee. */
    private boolean estDepassee(Demande demande) {
        return demande.getDateLimite() != null
                && demande.getDateLimite().isBefore(LocalDate.now());
    }

    /** Le role RH traite les demandes creees, la Finance et la DG relaient. */
    private boolean peutAgir(TypeRole role, StatutDemande courant, int idEmploye, Demande demande) {
        return switch (courant) {
            case CREE -> role == TypeRole.RH;
            case VALIDER_RH -> role == TypeRole.FINANCE;
            case VALIDER_FINANCE -> role == TypeRole.DG;
            case A_REVOIR -> role == TypeRole.MANAGER && demande.getIdEmployer() == idEmploye;
            default -> false;
        };
    }

    /** Seul l'interlocuteur de l'etage courant peut demander une revision. */
    private boolean peutDemanderRevision(TypeRole role, StatutDemande courant) {
        return (role == TypeRole.RH && courant == StatutDemande.CREE)
                || (role == TypeRole.FINANCE && courant == StatutDemande.VALIDER_RH)
                || (role == TypeRole.DG && courant == StatutDemande.VALIDER_FINANCE);
    }

    /** Statut a appliquer apres acceptation : celui de l'etage demandeur. */
    private StatutDemande statutApresRevision(int idDemande) {
        HistoriqueDemandeStatus revision = null;
        List<HistoriqueDemandeStatus> historique = historiqueRepository
                .findByIdDemandeOrderByDateAsc(idDemande);
        for (int i = historique.size() - 1; i >= 0; i--) {
            HistoriqueDemandeStatus ligne = historique.get(i);
            if (ligne.getStatus() != null
                    && StatutDemande.A_REVOIR.getLibelle().equalsIgnoreCase(ligne.getStatus().getLibelle())) {
                revision = ligne;
                break;
            }
        }
        if (revision == null || revision.getIdEmploye() == null) {
            throw new IllegalStateException("Impossible de retracer l'auteur de la revision.");
        }
        TypeRole roleDemandeur = roleDeEmploye(revision.getIdEmploye());
        if (roleDemandeur == null) {
            throw new IllegalStateException("Le role de l'auteur de la revision est inconnu.");
        }
        StatutDemande cible = StatutDemande.statutValidePar(roleDemandeur);
        if (cible == null) {
            throw new IllegalStateException("Le role de l'auteur de la revision est inconnu.");
        }
        return cible;
    }

    private TypeRole roleDeRole(int idRole) {
        if (idRole <= 0) {
            return null;
        }
        Role role = roleRepository.findOneById(idRole);
        return role != null ? TypeRole.fromLibelle(role.getLibelle()) : null;
    }

    private TypeRole roleDeEmploye(int idEmploye) {
        if (idEmploye <= 0) {
            return null;
        }
        Utilisateur utilisateur = utilisateurRepository.findByEmployer_Id(idEmploye);
        if (utilisateur == null || utilisateur.getIdRole() <= 0) {
            return null;
        }
        return roleDeRole(utilisateur.getIdRole());
    }

    /** Vrai si l'employe portant ce role est le createur de la demande. */
    private boolean estDemandeur(Demande demande, int idRole) {
        for (Utilisateur utilisateur : utilisateurRepository.findByIdRole(idRole)) {
            if (utilisateur.getEmployer() != null
                    && utilisateur.getEmployer().getId() == demande.getIdEmployer()) {
                return true;
            }
        }
        return false;
    }

    private List<Integer> idsDetails(int idDemande) {
        List<Integer> ids = new ArrayList<>();
        for (DetailDemande detail : detailRepository.findByIdDemande(idDemande)) {
            ids.add(detail.getId());
        }
        return ids;
    }

    private boolean dansPeriode(LocalDate date, LocalDate dateMin, LocalDate dateMax) {
        if (date == null) {
            return true;
        }
        if (dateMin != null && date.isBefore(dateMin)) {
            return false;
        }
        return dateMax == null || !date.isAfter(dateMax);
    }

    /** Ligne de detail proposee a la correction. */
    public record LigneRevision(int idDemandeDetails, Integer nbrPersonnelPropose) {
    }
}
