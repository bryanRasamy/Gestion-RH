package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class ContratWorkflowService {

    @Autowired
    private ContratRepository contratRepo;
    @Autowired
    private ContratStatutRepository contratStatutRepo;
    @Autowired
    private HistoriqueContratStatutRepository historiqueRepo;
    @Autowired
    private PeriodeEmploiRepository periodeEmploiRepo;
    @Autowired
    private PeriodeEmploiStatutRepository periodeStatutRepo;
    @Autowired
    private AffectationRepository affectationRepo;
    @Autowired
    private AffectationStatutRepository affectationStatutRepo;
    @Autowired
    private BadgeRepository badgeRepo;
    @Autowired
    private BadgeStatutRepository badgeStatutRepo;
    @Autowired
    private OnboardingTacheTypeRepository tacheTypeRepo;
    @Autowired
    private OnboardingTacheStatutRepository tacheStatutRepo;
    @Autowired
    private OnboardingTacheRepository tacheRepo;
    @Autowired
    private UtilisateurRepository utilisateurRepo;
    @Autowired
    private PaiePeriodeRepository paieRepo;
    @Autowired
    private EmployerRepository employerRepo;

    @Autowired private ContratTypeRepository contratTypeRepo;


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

    /**
     * Validation par le DG : statut SIGNE_PAR_CANDIDAT → VALIDE_PAR_DG + onboarding
     */
    @Transactional
    public Contrat validerParDg(int idContrat, int idUtilisateurDg) throws Exception {
        Contrat c = contratRepo.findById(idContrat)
                .orElseThrow(() -> new Exception("Contrat introuvable"));

        if (!"SIGNE_PAR_CANDIDAT".equals(c.getStatut().getLibelle())) {
            throw new Exception("Ce contrat n'est pas en attente de validation DG.");
        }

        Candidat candidat = c.getProposition().getCandidat();
        Utilisateur u = candidat.getUtilisateur();
        if (u == null) {
            throw new Exception("Utilisateur introuvable.");
        }

        Employer employer = u.getEmployer();
        if (employer == null) {
            employer = new Employer();
            employer.setMatricule(genererMatricule());
            employer.setNom(candidat.getNom());
            employer.setPrenom(candidat.getPrenom());
            employer.setTelephone(candidat.getTelephone());
            employer.setCompte(candidat.getEmail());
            employer.setPoste(candidat.getDetailDemande().getPoste());
            employer.setSalaire(c.getSalaire().doubleValue());
            employer = employerRepo.save(employer);

            u.setEmployer(employer);
            utilisateurRepo.save(u);
        }

        ContratStatut valide = contratStatutRepo.findByLibelle("VALIDE_PAR_DG");
        c.setStatut(valide);
        contratRepo.save(c);
        ajouterHistorique(c, valide, idUtilisateurDg, "Validé par la Direction Générale");

        PeriodeEmploi periode = new PeriodeEmploi();
        periode.setEmployer(employer);
        periode.setDateDebut(c.getDateDebut());
        periode.setStatut(periodeStatutRepo.findByLibelle("ACTIVE"));
        periode = periodeEmploiRepo.save(periode);

        // 4bis. Lier le contrat actuel à la période
        periode.setContratActuel(c);
        periodeEmploiRepo.save(periode);

        Affectation affectation = new Affectation();
        affectation.setPeriodeEmploi(periode);
        affectation.setPoste(candidat.getDetailDemande().getPoste());
        affectation.setContrat(c);
        affectation.setDateDebut(c.getDateDebut());
        affectation.setStatut(affectationStatutRepo.findByLibelle("EN_ATTENTE_PREREQUIS"));
        affectationRepo.save(affectation);

        Badge badge = new Badge();
        badge.setEmployer(employer);
        badge.setNumero(genererNumeroBadge(employer));
        badge.setDateEmission(LocalDateTime.now());
        badge.setStatut(badgeStatutRepo.findByLibelle("ACTIF"));
        badgeRepo.save(badge);

        List<OnboardingTacheType> types = tacheTypeRepo.findByActifTrueOrderByOrdre();
        OnboardingTacheStatut aFaire = tacheStatutRepo.findByLibelle("A_FAIRE");
        for (OnboardingTacheType type : types) {
            OnboardingTache t = new OnboardingTache();
            t.setPeriodeEmploi(periode);
            t.setType(type);
            t.setStatut(aFaire);
            tacheRepo.save(t);
        }

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

    private String genererMatricule() {
        long total = employerRepo.count() + 1;
        String matricule;
        do {
            matricule = String.format("EMP%03d", total);
            total++;
        } while (employerRepo.existsByMatricule(matricule));
        return matricule;
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

    @Transactional
    public Contrat demanderRenouvellement(int idContratPrecedent,
            LocalDateTime nouvelleDateDebut,
            LocalDateTime nouvelleDateFin,
            BigDecimal nouveauSalaire,
            String nouveauTypeLibelle,
            int idUtilisateur) throws Exception {

        Contrat ancien = contratRepo.findById(idContratPrecedent)
                .orElseThrow(() -> new Exception("Contrat précédent introuvable."));

        if (!"VALIDE_PAR_DG".equals(ancien.getStatut().getLibelle())) {
            throw new Exception("Seul un contrat validé par le DG peut être renouvelé.");
        }

        // Retrouver la période d'emploi via l'affectation liée
        Affectation affectationAncienne = affectationRepo.findAll().stream()
                .filter(a -> a.getContrat() != null && a.getContrat().getId() == ancien.getId())
                .findFirst()
                .orElseThrow(() -> new Exception("Affectation du contrat précédent introuvable."));

        PeriodeEmploi periode = affectationAncienne.getPeriodeEmploi();

        // Vérifier les dates
        if (nouvelleDateDebut.isBefore(ancien.getDateDebut())) {
            throw new Exception("La nouvelle date de début doit être après celle du contrat précédent.");
        }
        if (ancien.getDateFin() != null && nouvelleDateDebut.isBefore(ancien.getDateFin())) {
            throw new Exception("La nouvelle date de début doit être postérieure à la fin du contrat précédent.");
        }

        // Type de contrat
        ContratType type = contratTypeRepo.findByLibelle(nouveauTypeLibelle);
        if (type == null)
            throw new Exception("Type de contrat inconnu : " + nouveauTypeLibelle);

        // Si date fin null (CDI), elle doit rester null
        if (type.getDureeMaxMois() == null) {
            nouvelleDateFin = null;
        } else if (nouvelleDateFin == null) {
            throw new Exception("Ce type de contrat exige une date de fin.");
        }

        // Créer le nouveau contrat
        Contrat nouveau = new Contrat();
        nouveau.setProposition(ancien.getProposition()); // même candidat/proposition
        nouveau.setContratType(type);
        nouveau.setDateDebut(nouvelleDateDebut);
        nouveau.setDateFin(nouvelleDateFin);
        nouveau.setSalaire(nouveauSalaire);
        nouveau.setStatut(contratStatutRepo.findByLibelle("CREE"));
        nouveau = contratRepo.save(nouveau);

        // Historique
        HistoriqueContratStatut h = new HistoriqueContratStatut();
        h.setContrat(nouveau);
        h.setStatut(contratStatutRepo.findByLibelle("CREE"));
        h.setMotif("Renouvellement — contrat précédent #" + ancien.getId());
        h.setDateChangement(LocalDateTime.now());
        historiqueRepo.save(h);

        // Créer une nouvelle affectation liée à la même période, liée au nouveau
        // contrat
        Affectation nouvelleAffectation = new Affectation();
        nouvelleAffectation.setPeriodeEmploi(periode);
        nouvelleAffectation.setPoste(affectationAncienne.getPoste());
        nouvelleAffectation.setContrat(nouveau);
        nouvelleAffectation.setDateDebut(nouvelleDateDebut);
        nouvelleAffectation.setStatut(affectationStatutRepo.findByLibelle("EN_ATTENTE_PREREQUIS"));
        affectationRepo.save(nouvelleAffectation);

        return nouveau;
    }

    /** Signature du nouveau contrat par l'employé (CREE → SIGNE_PAR_CANDIDAT). */
    @Transactional
    public Contrat signerContrat(int idContrat) throws Exception {
        Contrat c = contratRepo.findById(idContrat)
                .orElseThrow(() -> new Exception("Contrat introuvable."));
        if (!"CREE".equals(c.getStatut().getLibelle())) {
            throw new Exception("Ce contrat ne peut pas être signé dans son état actuel.");
        }
        ContratStatut nouveau = contratStatutRepo.findByLibelle("SIGNE_PAR_CANDIDAT");
        c.setStatut(nouveau);
        contratRepo.save(c);
        ajouterHistorique(c, nouveau, null, "Signé par l'employé");
        return c;
    }

    /**
     * Validation du renouvellement par le DG : statut SIGNE_PAR_CANDIDAT → VALIDE_PAR_DG
     */
    @Transactional
    public Contrat validerRenouvellementParDg(int idContrat, int idUtilisateurDg) throws Exception {
        Contrat c = contratRepo.findById(idContrat)
                .orElseThrow(() -> new Exception("Contrat introuvable."));

        if (!"SIGNE_PAR_CANDIDAT".equals(c.getStatut().getLibelle())) {
            throw new Exception("Ce contrat n'est pas en attente de validation DG.");
        }

        Affectation affectation = affectationRepo.findAll().stream()
                .filter(a -> a.getContrat() != null && a.getContrat().getId() == c.getId())
                .findFirst()
                .orElseThrow(() -> new Exception("Aucune affectation liée à ce contrat."));

        PeriodeEmploi periode = affectation.getPeriodeEmploi();

        ContratStatut valide = contratStatutRepo.findByLibelle("VALIDE_PAR_DG");
        c.setStatut(valide);
        contratRepo.save(c);
        ajouterHistorique(c, valide, idUtilisateurDg, "Renouvellement validé par le DG");

        affectation.setStatut(affectationStatutRepo.findByLibelle("ACTIVE"));
        affectationRepo.save(affectation);

        periode.setContratActuel(c);
        periodeEmploiRepo.save(periode);

        Employer employer = periode.getEmployer();
        employer.setSalaire(c.getSalaire().doubleValue());
        employerRepo.save(employer);

        LocalDateTime now = LocalDateTime.now();
        int annee = now.getYear();
        int mois = now.getMonthValue();
        if (!paieRepo.existsByEmployerIdAndAnneeAndMois(employer.getId(), annee, mois)) {
            PaiePeriode pp = new PaiePeriode();
            pp.setEmployer(employer);
            pp.setAnnee(annee);
            pp.setMois(mois);
            pp.setMontant(c.getSalaire());
            pp.setDatePaiement(now);
            pp.setCommentaire("Renouvellement de contrat #" + c.getId());
            paieRepo.save(pp);
        }

        return c;
    }
}
