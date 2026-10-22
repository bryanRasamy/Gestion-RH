package gestion.entreprise.service;

import gestion.entreprise.modele.Demande;
import gestion.entreprise.modele.DemandeARevoir;
import gestion.entreprise.modele.DetailDemande;
import gestion.entreprise.modele.HistoriqueDemandeStatus;
import gestion.entreprise.modele.Role;
import gestion.entreprise.modele.Status;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.DemandeARevoirRepository;
import gestion.entreprise.repository.DemandeRepository;
import gestion.entreprise.repository.DetailDemandeRepository;
import gestion.entreprise.repository.HistoriqueDemandeStatusRepository;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.repository.StatusRepository;
import gestion.entreprise.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class StatutDemande {
    private final DemandeRepository demandeRepository;
    private final DetailDemandeRepository detailRepository;
    private final HistoriqueDemandeStatusRepository historiqueRepository;
    private final DemandeARevoirRepository revoirRepository;
    private final StatusRepository statusRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final BudgetService budgetService;

    public StatutDemande(DemandeRepository demandeRepository, DetailDemandeRepository detailRepository,
                         HistoriqueDemandeStatusRepository historiqueRepository,
                         DemandeARevoirRepository revoirRepository, StatusRepository statusRepository,
                         UtilisateurRepository utilisateurRepository, RoleRepository roleRepository,
                         BudgetService budgetService) {
        this.demandeRepository = demandeRepository;
        this.detailRepository = detailRepository;
        this.historiqueRepository = historiqueRepository;
        this.revoirRepository = revoirRepository;
        this.statusRepository = statusRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.budgetService = budgetService;
    }

    @Transactional
    public void expirerDemandes() {
        List<Demande> demandes = demandeRepository.findByDateLimiteBeforeAndTraiteFalse(LocalDate.now());
        Status expire = statut("expire");
        for (Demande demande : demandes) {
            HistoriqueDemandeStatus courant = courant(demande.getId());
            if (courant != null && estExpirable(courant)) {
                ajouterStatut(demande, expire, null, "Expiration automatique");
                demande.setTraite(true);
                demandeRepository.save(demande);
            }
        }
    }

    public List<Demande> lister(int idRole, LocalDate min, LocalDate max, String libelleStatut) {
        expirerDemandes();
        List<Demande> resultat = new ArrayList<Demande>();
        List<Demande> demandes = demandeRepository.findAll();
        for (Demande demande : demandes) {
            HistoriqueDemandeStatus courant = courant(demande.getId());
            if (!estAccessible(courant, idRole)) continue;
            if (libelleStatut != null && !libelleStatut.isBlank()) {
                if (courant == null || !libelleStatut.equalsIgnoreCase(courant.getLibelle())) continue;
            }
            if (courant != null) {
                if (min != null && courant.getDate().isBefore(min)) continue;
                if (max != null && courant.getDate().isAfter(max)) continue;
            }
            resultat.add(demande);
        }
        return resultat;
    }

    @Transactional
    public void traiter(int idDemande, int idEmploye, int idRole, String decision,
                        String motif, Integer propose) {
        Demande demande = demandeRepository.findOneById(idDemande);
        if (demande == null) throw new IllegalArgumentException("Demande introuvable.");

        if (demande.getDateLimite() != null && demande.getDateLimite().isBefore(LocalDate.now())) {
            expirerDemandes();
            throw new IllegalStateException("La demande est expirée et ne peut plus être traitée.");
        }

        HistoriqueDemandeStatus dernier = courant(idDemande);
        if (dernier == null) {
            throw new IllegalStateException("La demande n'a pas de statut courant.");
        }
        if (estFinal(dernier.getLibelle())) {
            throw new IllegalStateException("La demande est déjà clôturée.");
        }
        if (!estAccessible(dernier, idRole)) {
            throw new IllegalStateException("La demande a deja ete traitee par une autre etape.");
        }
        if (decision == null || decision.isBlank()) {
            throw new IllegalArgumentException("La decision est obligatoire.");
        }
        String decisionNormalisee = normaliser(decision);
        if (estRefus(decisionNormalisee) && (motif == null || motif.isBlank())) {
            throw new IllegalArgumentException("Le motif du refus est obligatoire.");
        }
        if (estValider(decisionNormalisee) && roleEst(idRole, "Finance")
                && !budgetService.estDansBudget(idDemande)) {
            throw new IllegalStateException("Le budget annuel ne permet pas cette validation.");
        }
        if (estARevoir(decisionNormalisee)) {
            demanderRevision(demande, idEmploye, motif, propose);
            return;
        }

        Status statut = estRefus(decisionNormalisee) ? statut("refuser", "refuse") : statut("valider");
        if (statut == null) throw new IllegalArgumentException("Statut inconnu dans la base.");
        ajouterStatut(demande, statut, idEmploye, motif);
        if (estRefus(statut.getLibelle()) || roleEst(idRole, "DG")) {
            demande.setTraite(true);
            demandeRepository.save(demande);
        }
    }

    @Transactional
    public void demanderRevision(Demande demande, int idEmploye, String commentaire, Integer propose) {
        List<DetailDemande> details = detailRepository.findByIdDemande(demande.getId());
        for (DetailDemande detail : details) {
            DemandeARevoir retour = new DemandeARevoir();
            retour.setIdDemandeur(demande.getIdEmployer());
            retour.setIdDemandeDetails(detail.getId());
            retour.setDate(LocalDate.now());
            retour.setCommentaire(commentaire);
            retour.setNbrPersonnelPropose(propose);
            revoirRepository.save(retour);
        }
        ajouterStatut(demande, statut("a revoir", "a_revoir"), idEmploye, commentaire);
    }

    public Status statut(String... libelles) {
        if (libelles == null) return null;
        for (String libelle : libelles) {
            if (libelle == null || libelle.isBlank()) continue;
            Status status = statusRepository.findByLibelleIgnoreCase(libelle);
            if (status != null) return status;
        }
        return null;
    }

    private HistoriqueDemandeStatus courant(int idDemande) {
        return historiqueRepository.findTopByIdDemandeOrderByDateDescIdDesc(idDemande);
    }

    private boolean estExpirable(HistoriqueDemandeStatus historique) {
        String libelle = normaliser(historique.getLibelle());
        return estCree(libelle) || estValider(libelle) || estARevoir(libelle);
    }

    private boolean estAccessible(HistoriqueDemandeStatus historique, int idRole) {
        if (historique == null) return false;

        String libelle = normaliser(historique.getLibelle());
        if (estCree(libelle)) {
            return roleEst(idRole, "RH");
        }
        if (estValider(libelle)) {
            return prochaineEtape(historique.getIdEmploye(), idRole);
        }
        return false;
    }

    private boolean prochaineEtape(Integer idEmploye, int idRole) {
        if (idEmploye == null) return false;
        Utilisateur acteur = utilisateurRepository.findByEmployer_Id(idEmploye);
        if (acteur == null) return false;
        Role roleActeur = roleRepository.findOneById(acteur.getIdRole());
        Role roleCourant = roleRepository.findOneById(idRole);
        if (roleActeur == null || roleCourant == null) return false;
        if ("RH".equalsIgnoreCase(roleActeur.getLibelle())) return "Finance".equalsIgnoreCase(roleCourant.getLibelle());
        if ("Finance".equalsIgnoreCase(roleActeur.getLibelle())) return "DG".equalsIgnoreCase(roleCourant.getLibelle());
        return false;
    }

    private boolean roleEst(int idRole, String libelle) {
        Role role = roleRepository.findOneById(idRole);
        return role != null && libelle.equalsIgnoreCase(role.getLibelle());
    }

    private void ajouterStatut(Demande demande, Status statut, Integer idEmploye, String motif) {
        if (statut == null) throw new IllegalStateException("Le statut demande est absent de la base.");
        HistoriqueDemandeStatus historique = new HistoriqueDemandeStatus();
        historique.setIdDemande(demande.getId());
        historique.setLibelle(statut.getLibelle());
        historique.setIdEmploye(idEmploye);
        historique.setDate(LocalDate.now());
        historique.setMotif(motif);
        historiqueRepository.save(historique);
    }

    private boolean estFinal(String libelle) {
        String statut = normaliser(libelle);
        return "expire".equals(statut)
                || "abandone".equals(statut)
                || estRefus(statut);
    }

    private String normaliser(String valeur) {
        if (valeur == null) return "";
        return valeur.trim().toLowerCase(Locale.ROOT)
                .replace("à", "a")
                .replace('_', ' ');
    }

    private boolean estValider(String libelle) {
        return "valider".equals(normaliser(libelle)) || "valide".equals(normaliser(libelle));
    }

    private boolean estARevoir(String libelle) {
        return "a revoir".equals(normaliser(libelle));
    }

    private boolean estRefus(String libelle) {
        String statut = normaliser(libelle);
        return "refuser".equals(statut) || "refuse".equals(statut);
    }

    private boolean estCree(String libelle) {
        String statut = normaliser(libelle);
        return "cree".equals(statut) || "creer".equals(statut) || "cree".equals(statut);
    }
}
