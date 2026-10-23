package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PropositionEmbaucheService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", Locale.FRENCH);
    private static final DecimalFormat NUM = new DecimalFormat("#,##0");

    @Autowired
    private PropositionEmbaucheRepository propositionRepo;
    @Autowired
    private PropositionEmbaucheQuestionRepository questionRepo;
    @Autowired
    private PropositionEmbaucheStatutRepository statutRepo;
    @Autowired
    private ContratTypeRepository contratTypeRepo;
    @Autowired
    private ReponseTypeRepository reponseTypeRepo;
    @Autowired
    private PropositionEmbaucheNegociationRepository negociationRepo;
    @Autowired
    private HistoriquePropositionEmbaucheStatutRepository historiqueRepo;
    @Autowired
    private ContratRepository contratRepo;
    @Autowired
    private ContratStatutRepository contratStatutRepo;
    @Autowired
    private HistoriqueContratStatutRepository historiqueContratStatutRepo;
    @Autowired
    private CandidatRepository candidatRepo;

    @Transactional
    public PropositionEmbauche creerProposition(int idCandidat, BigDecimal salaire,
            LocalDateTime dateDebut, String lieu, String horaires) throws Exception {
        Candidat c = candidatRepo.findById(idCandidat)
                .orElseThrow(() -> new Exception("Candidat introuvable"));
        if (!"ACCEPTE".equals(c.getStatus())) {
            throw new Exception("Le candidat doit être accepté après entretien.");
        }

        PropositionEmbauche p = new PropositionEmbauche();
        p.setCandidat(c);
        p.setContratType(contratTypeRepo.findByLibelle("CE"));
        p.setSalaire(salaire);
        p.setDateDebut(dateDebut);
        p.setLieuTravail(lieu);
        p.setHoraires(horaires);
        p.setDureeEssaiMois(6);
        p.setDateExpiration(LocalDateTime.now().plusDays(7));
        p.setStatut(statutRepo.findByLibelle("EN_ATTENTE_CANDIDAT"));
        p = propositionRepo.save(p);

        // 4 questions (5 et 6 retirées)
        String[] questions = {
                "Salaire proposé : " + NUM.format(salaire) + " Ar brut/mois. Acceptez-vous ce salaire ?",
                "Date de début : " + dateDebut.format(FMT) + ". Acceptez-vous cette date ?",
                "Lieu de travail : " + lieu + ". Acceptez-vous ce lieu ?",
                "Horaires : " + horaires + ". Acceptez-vous ces horaires ?"
        };

        for (int i = 0; i < questions.length; i++) {
            PropositionEmbaucheQuestion q = new PropositionEmbaucheQuestion();
            q.setProposition(p);
            q.setOrdre(i + 1);
            q.setQuestion(questions[i]);
            questionRepo.save(q);
        }

        ajouterHistorique(p, "EN_ATTENTE_CANDIDAT", null, "Proposition créée");
        return p;
    }

    /**
     * Réponse du candidat.
     * 
     * @param action      ACCEPTER | NEGOCIER | REFUSER
     * @param motifAction remarques/motif pour NEGOCIER/REFUSER
     */
    @Transactional
    public PropositionEmbauche repondre(int idProposition, String action,
            List<Integer> questionIds, List<String> reponses,
            List<String> remarques, String contratChoisi,
            String motifAction) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition)
                .orElseThrow(() -> new Exception("Proposition introuvable"));

        if (!"EN_ATTENTE_CANDIDAT".equals(p.getStatut().getLibelle())) {
            throw new Exception("Cette proposition n'est plus en attente.");
        }
        if (action == null)
            throw new Exception("Action manquante.");

        // Validation tailles
        if (questionIds == null || reponses == null || questionIds.size() != reponses.size()) {
            throw new Exception("Données invalides.");
        }

        // Sauvegarde des réponses
        boolean toutesOui = true;
        for (int i = 0; i < questionIds.size(); i++) {
            Integer idQ = questionIds.get(i);
            PropositionEmbaucheQuestion q = questionRepo.findById(idQ).orElse(null);
            if (q == null)
                throw new Exception("Question introuvable: " + idQ);

            String rep = reponses.get(i);
            String rem = (remarques != null && i < remarques.size()) ? remarques.get(i) : null;

            ReponseType rt = reponseTypeRepo.findByLibelle(rep);
            if (rt == null)
                throw new Exception("Réponse invalide: " + rep);

            q.setReponse(rt);
            q.setRemarque(rem);
            questionRepo.save(q);

            if (!"OUI".equalsIgnoreCase(rep))
                toutesOui = false;
        }

        // Validation du contrat choisi
        if (contratChoisi == null || contratChoisi.isBlank())
            contratChoisi = "CE";
        ContratType ct = contratTypeRepo.findByLibelle(contratChoisi);
        if (ct == null)
            throw new Exception("Type de contrat invalide: " + contratChoisi);
        p.setContratType(ct);

        switch (action.toUpperCase()) {
            case "ACCEPTER":
                // GUARD : toutes les réponses doivent être OUI
                if (!toutesOui) {
                    throw new Exception("Vous devez accepter toutes les conditions pour accepter l'offre.");
                }
                if ("CE".equalsIgnoreCase(contratChoisi)) {
                    // Auto-accept : contrat CE
                    p.setStatut(statutRepo.findByLibelle("ACCEPTEE"));
                    propositionRepo.save(p);
                    ajouterHistorique(p, "ACCEPTEE", null, "Acceptation directe (CE)");
                    creerContrat(p);
                } else {
                    // Non-CE : validation RH requise
                    p.setStatut(statutRepo.findByLibelle("EN_VALIDATION_RH"));
                    propositionRepo.save(p);
                    ajouterHistorique(p, "EN_VALIDATION_RH", null,
                            "Contrat " + contratChoisi + " — en attente de validation RH");
                }
                break;

            case "NEGOCIER":
                StringBuilder motifNego = new StringBuilder();
                if (motifAction != null && !motifAction.trim().isEmpty()) {
                    motifNego.append(motifAction.trim());
                } else {
                    for (PropositionEmbaucheQuestion q : questionRepo.findByPropositionIdOrderByOrdre(p.getId())) {
                        if (q.getRemarque() != null && !q.getRemarque().trim().isEmpty()) {
                            if (motifNego.length() > 0)
                                motifNego.append(" | ");
                            motifNego.append(q.getRemarque().trim());
                        }
                    }
                }
                if (motifNego.length() == 0) {
                    throw new Exception("Pour demander une négociation, veuillez renseigner au moins une remarque.");
                }

                PropositionEmbaucheNegociation n = new PropositionEmbaucheNegociation();
                n.setProposition(p);
                n.setRemarquesCandidat(motifNego.toString());
                negociationRepo.save(n);

                p.setStatut(statutRepo.findByLibelle("EN_NEGOCIATION"));
                propositionRepo.save(p);
                ajouterHistorique(p, "EN_NEGOCIATION", null, "Négociation demandée: " + motifNego);
                break;

            case "REFUSER":
                if (motifAction == null || motifAction.trim().isEmpty()) {
                    throw new Exception("Le motif de refus est obligatoire.");
                }
                p.setStatut(statutRepo.findByLibelle("REFUSEE"));
                propositionRepo.save(p);
                ajouterHistorique(p, "REFUSEE", null, "Refus: " + motifAction.trim());
                break;

            default:
                throw new Exception("Action inconnue: " + action);
        }

        return p;
    }

    /**
     * Candidat confirme après négociation acceptée par RH.
     */
    @Transactional
    public PropositionEmbauche confirmerFinal(int idProposition, String action) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition)
                .orElseThrow(() -> new Exception("Proposition introuvable"));

        if (!"EN_ATTENTE_FINAL".equals(p.getStatut().getLibelle())) {
            throw new Exception("Cette proposition n'est pas en attente de confirmation finale.");
        }

        if ("ACCEPTER".equalsIgnoreCase(action)) {
            p.setStatut(statutRepo.findByLibelle("ACCEPTEE"));
            propositionRepo.save(p);
            ajouterHistorique(p, "ACCEPTEE", null, "Confirmation finale du candidat");
            creerContrat(p);
        } else if ("REFUSER".equalsIgnoreCase(action)) {
            p.setStatut(statutRepo.findByLibelle("REFUSEE"));
            propositionRepo.save(p);
            ajouterHistorique(p, "REFUSEE", null, "Abandon après négociation");
        } else {
            throw new Exception("Action invalide.");
        }
        return p;
    }

    @Transactional
    public void validerParRh(int idProposition, boolean accepte, String reponseRh, int idUtilisateur) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition).orElseThrow();

        String statutActuel = p.getStatut().getLibelle();
        if (!"EN_NEGOCIATION".equals(statutActuel) && !"EN_VALIDATION_RH".equals(statutActuel)) {
            throw new Exception("Cette proposition n'est pas en attente de validation RH.");
        }

        if ("EN_NEGOCIATION".equals(statutActuel)) {
            if (accepte) {
                // RH accepte la négociation → candidat doit confirmer
                p.setStatut(statutRepo.findByLibelle("EN_ATTENTE_FINAL"));
                propositionRepo.save(p);
                ajouterHistorique(p, "EN_ATTENTE_FINAL", idUtilisateur, reponseRh);
            } else {
                p.setStatut(statutRepo.findByLibelle("REFUSEE"));
                propositionRepo.save(p);
                ajouterHistorique(p, "REFUSEE", idUtilisateur, reponseRh);
            }
            List<PropositionEmbaucheNegociation> negs = p.getNegociations();
            if (negs != null && !negs.isEmpty()) {
                PropositionEmbaucheNegociation derniere = negs.get(negs.size() - 1);
                derniere.setAccepte(accepte);
                derniere.setReponseRh(reponseRh);
                negociationRepo.save(derniere);
            }
        } else { // EN_VALIDATION_RH
            if (accepte) {
                p.setStatut(statutRepo.findByLibelle("ACCEPTEE"));
                propositionRepo.save(p);
                ajouterHistorique(p, "ACCEPTEE", idUtilisateur, reponseRh);
                creerContrat(p);
            } else {
                p.setStatut(statutRepo.findByLibelle("REFUSEE"));
                propositionRepo.save(p);
                ajouterHistorique(p, "REFUSEE", idUtilisateur, reponseRh);
            }
        }
    }

    /** Crée le contrat si accepté et n'existe pas encore. */
    private void creerContrat(PropositionEmbauche p) {
        if (contratRepo.findFirstByPropositionId(p.getId()) != null)
            return;

        Contrat c = new Contrat();
        c.setProposition(p);
        c.setContratType(p.getContratType());
        c.setDateDebut(p.getDateDebut());
        c.setSalaire(p.getSalaire());
        c.setStatut(contratStatutRepo.findByLibelle("CREE"));

        Integer dureeMois = p.getContratType().getDureeMaxMois();
        if (dureeMois != null) {
            c.setDateFin(p.getDateDebut().plusMonths(dureeMois));
        }
        Contrat saved = contratRepo.save(c);

        HistoriqueContratStatut h = new HistoriqueContratStatut();
        h.setContrat(saved);
        h.setStatut(contratStatutRepo.findByLibelle("CREE"));
        h.setMotif("Contrat généré automatiquement à l'acceptation");
        h.setDateChangement(LocalDateTime.now());
        historiqueContratStatutRepo.save(h);
    }

    @Transactional
    public void abandonnerCandidat(int idProposition) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition).orElseThrow();
        if ("ACCEPTEE".equals(p.getStatut().getLibelle()) || "REFUSEE".equals(p.getStatut().getLibelle())) {
            throw new Exception("Cette proposition est déjà clôturée.");
        }
        p.setStatut(statutRepo.findByLibelle("REFUSEE"));
        propositionRepo.save(p);
        ajouterHistorique(p, "REFUSEE", null, "Le candidat a abandonné");
    }

    private void ajouterHistorique(PropositionEmbauche p, String libelleStatut, Integer idUtilisateur, String motif) {
        HistoriquePropositionEmbaucheStatut h = new HistoriquePropositionEmbaucheStatut();
        h.setProposition(p);
        h.setStatut(statutRepo.findByLibelle(libelleStatut));
        if (idUtilisateur != null) {
            Utilisateur u = new Utilisateur();
            u.setId(idUtilisateur);
            h.setUtilisateur(u);
        }
        h.setMotif(motif);
        h.setDateChangement(LocalDateTime.now());
        historiqueRepo.save(h);
    }

    public PropositionEmbauche getProposition(int idCandidat) {
        return propositionRepo.findFirstByCandidatIdOrderByCreatedAtDesc(idCandidat);
    }

    public PropositionEmbauche getById(int id) {
        return propositionRepo.findById(id).orElse(null);
    }

    public List<PropositionEmbauche> getPropositions() {
        return propositionRepo.findAll();
    }

    public List<PropositionEmbaucheQuestion> getQuestions(int idProposition) {
        return questionRepo.findByPropositionIdOrderByOrdre(idProposition);
    }

    public List<HistoriquePropositionEmbaucheStatut> getHistorique(int idProposition) {
        return historiqueRepo.findByPropositionIdOrderByDateChangementAsc(idProposition);
    }
}