package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContratWorkflowService {

    @Autowired private ContratRepository contratRepo;
    @Autowired private ContratStatutRepository contratStatutRepo;
    @Autowired private HistoriqueContratStatutRepository historiqueRepo;
    @Autowired private PeriodeEmploiRepository periodeEmploiRepo;
    @Autowired private PeriodeEmploiStatutRepository periodeStatutRepo;
    @Autowired private AffectationRepository affectationRepo;
    @Autowired private AffectationStatutRepository affectationStatutRepo;
    @Autowired private BadgeRepository badgeRepo;
    @Autowired private BadgeStatutRepository badgeStatutRepo;
    @Autowired private OnboardingTacheTypeRepository tacheTypeRepo;
    @Autowired private OnboardingTacheStatutRepository tacheStatutRepo;
    @Autowired private OnboardingTacheRepository tacheRepo;
    @Autowired private UtilisateurRepository utilisateurRepo;
    @Autowired private PaiePeriodeRepository paieRepo;

    /** Signature par le candidat (statut CREE → SIGNE_PAR_CANDIDAT) */
    @Transactional
    public Contrat signerParCandidat(int idContrat) throws Exception {
        Contrat c = contratRepo.findById(idContrat)
                .orElseThrow(() -> new Exception("Contrat introuvable"));

        if (!"CREE".equals(c.getStatut().getLibelle())) {
            throw new Exception("Ce contrat ne peut pas être signé dans son état actuel.");
        }

        ContratStatut nouveau = contratStatutRepo.findByLibelle("SIGNE_PAR_CANDIDAT");
        c.setStatut(nouveau);
        contratRepo.save(c);

        ajouterHistorique(c, nouveau, null, "Signé par le candidat");
        return c;
    }

    /** Validation par le DG : statut SIGNE_PAR_CANDIDAT → VALIDE_PAR_DG + onboarding */
    @Transactional
    public Contrat validerParDg(int idContrat, int idUtilisateurDg) throws Exception {
        Contrat c = contratRepo.findById(idContrat)
                .orElseThrow(() -> new Exception("Contrat introuvable"));

        if (!"SIGNE_PAR_CANDIDAT".equals(c.getStatut().getLibelle())) {
            throw new Exception("Ce contrat n'est pas en attente de validation DG.");
        }

        // 1. Changer le statut du contrat
        ContratStatut valide = contratStatutRepo.findByLibelle("VALIDE_PAR_DG");
        c.setStatut(valide);
        contratRepo.save(c);
        ajouterHistorique(c, valide, idUtilisateurDg, "Validé par la Direction Générale");

        // 2. Récupérer l'employer lié au candidat
        Candidat candidat = c.getProposition().getCandidat();
        Utilisateur u = candidat.getUtilisateur();
        Employer employer = u.getEmployer();
        if (employer == null) {
            throw new IllegalStateException("Aucun employer associé à cet utilisateur.");
        }

        // 3. Créer la période d'emploi (ACTIVE)
        PeriodeEmploi periode = new PeriodeEmploi();
        periode.setEmployer(employer);
        periode.setDateDebut(c.getDateDebut());
        periode.setStatut(periodeStatutRepo.findByLibelle("ACTIVE"));
        periode = periodeEmploiRepo.save(periode);

        // 4. Créer l'affectation (EN_ATTENTE_PREREQUIS)
        Affectation affectation = new Affectation();
        affectation.setPeriodeEmploi(periode);
        affectation.setPoste(candidat.getDetailDemande().getPoste());
        affectation.setContrat(c);
        affectation.setDateDebut(c.getDateDebut());
        affectation.setStatut(affectationStatutRepo.findByLibelle("EN_ATTENTE_PREREQUIS"));
        affectationRepo.save(affectation);

        // 5. Créer le badge
        Badge badge = new Badge();
        badge.setEmployer(employer);
        badge.setNumero(genererNumeroBadge(employer));
        badge.setDateEmission(LocalDateTime.now());
        badge.setStatut(badgeStatutRepo.findByLibelle("ACTIF"));
        badgeRepo.save(badge);

        // 6. Générer les tâches d'onboarding
        List<OnboardingTacheType> types = tacheTypeRepo.findByActifTrueOrderByOrdre();
        OnboardingTacheStatut aFaire = tacheStatutRepo.findByLibelle("A_FAIRE");
        for (OnboardingTacheType type : types) {
            OnboardingTache t = new OnboardingTache();
            t.setPeriodeEmploi(periode);
            t.setType(type);
            t.setStatut(aFaire);
            tacheRepo.save(t);
        }

        // 7. Inscription automatique à la paie périodique (mois en cours)
        LocalDateTime now = LocalDateTime.now();
        int annee = now.getYear();
        int mois = now.getMonthValue();

        if (!paieRepo.existsByEmployerIdAndAnneeAndMois(employer.getId(), annee, mois)) {
            PaiePeriode paie = new PaiePeriode();
            paie.setEmployer(employer);
            paie.setAnnee(annee);
            paie.setMois(mois);
            paie.setMontant(c.getSalaire());
            paie.setDatePaiement(now);
            paie.setCommentaire("Paiement automatique — embauche validée par le DG");
            paieRepo.save(paie);
        }

        return c;
    }

    private String genererNumeroBadge(Employer employer) {
        String base = "BADGE-" + String.format("%04d", employer.getId());
        String numero = base;
        int suffix = 1;
        while (badgeRepo.existsByNumero(numero)) {
            numero = base + "-" + suffix++;
        }
        return numero;
    }

    private void ajouterHistorique(Contrat c, ContratStatut statut, Integer idUtilisateur, String motif) {
        HistoriqueContratStatut h = new HistoriqueContratStatut();
        h.setContrat(c);
        h.setStatut(statut);
        if (idUtilisateur != null) {
            Utilisateur u = new Utilisateur();
            u.setId(idUtilisateur);
            h.setUtilisateur(u);
        }
        h.setMotif(motif);
        h.setDateChangement(LocalDateTime.now());
        historiqueRepo.save(h);
    }

    public List<HistoriqueContratStatut> getHistorique(int idContrat) {
        return historiqueRepo.findByContratIdOrderByDateChangementAsc(idContrat);
    }

    /** Contrats en attente de validation DG */
    public List<Contrat> getContratsAValiderParDg() {
        ContratStatut signe = contratStatutRepo.findByLibelle("SIGNE_PAR_CANDIDAT");
        return contratRepo.findByStatutId(signe.getId());
    }
}