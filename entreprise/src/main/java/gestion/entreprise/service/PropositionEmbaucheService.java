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
public class PropositionEmbaucheService {

    @Autowired private PropositionEmbaucheRepository propositionRepo;
    @Autowired private PropositionEmbaucheQuestionRepository questionRepo;
    @Autowired private PropositionEmbaucheStatutRepository statutRepo;
    @Autowired private ContratTypeRepository contratTypeRepo;
    @Autowired private ReponseTypeRepository reponseTypeRepo;
    @Autowired private PropositionEmbaucheNegociationRepository negociationRepo;
    @Autowired private HistoriquePropositionEmbaucheStatutRepository historiqueRepo;
    @Autowired private CandidatRepository candidatRepo;

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

        String[] questions = {
            "Salaire proposé : " + salaire + " Ar brut/mois. Acceptez-vous ce salaire ?",
            "Date de début : " + dateDebut + ". Acceptez-vous cette date ?",
            "Lieu de travail : " + lieu + ". Acceptez-vous ce lieu ?",
            "Horaires : " + horaires + ". Acceptez-vous ces horaires ?",
            "Type de contrat proposé : CE (défaut). Souhaitez-vous un autre type ?",
            "Période d'essai : 6 mois renouvelable 1 fois. Acceptez-vous cette période ?"
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

    @Transactional
    public PropositionEmbauche repondre(int idProposition, List<Integer> questionIds,
                                        List<String> reponses, List<String> remarques,
                                        String contratChoisi) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition)
                .orElseThrow(() -> new Exception("Proposition introuvable"));

        if (!"EN_ATTENTE_CANDIDAT".equals(p.getStatut().getLibelle())) {
            throw new Exception("Cette proposition n'est plus en attente.");
        }

        boolean toutesOui = true;
        boolean negociation = false;

        for (int i = 0; i < questionIds.size(); i++) {
            PropositionEmbaucheQuestion q = questionRepo.findById(questionIds.get(i))
                    .orElseThrow(() -> new Exception("Question introuvable"));
            String rep = reponses.get(i);
            ReponseType rt = reponseTypeRepo.findByLibelle(rep);
            q.setReponse(rt);
            q.setRemarque(remarques.get(i));
            questionRepo.save(q);

            if (!"OUI".equalsIgnoreCase(rep)) {
                toutesOui = false;
                if ("AUTRE".equalsIgnoreCase(rep)) negociation = true;
            }
        }

        boolean contratCE = "CE".equalsIgnoreCase(contratChoisi);
        if (!contratCE) {
            p.setContratType(contratTypeRepo.findByLibelle(contratChoisi));
            propositionRepo.save(p);
        }

        if (toutesOui && contratCE) {
            p.setStatut(statutRepo.findByLibelle("ACCEPTEE"));
            propositionRepo.save(p);
            ajouterHistorique(p, "ACCEPTEE", null, "Toutes les réponses OUI + contrat CE");
        } else if (toutesOui && !contratCE) {
            p.setStatut(statutRepo.findByLibelle("EN_VALIDATION_RH"));
            propositionRepo.save(p);
            ajouterHistorique(p, "EN_VALIDATION_RH", null, "Contrat " + contratChoisi + " choisi");
        } else {
            PropositionEmbaucheNegociation n = new PropositionEmbaucheNegociation();
            n.setProposition(p);
            StringBuilder sb = new StringBuilder();
            for (String r : remarques) {
                if (r != null && !r.trim().isEmpty()) sb.append(r).append(" | ");
            }
            n.setRemarquesCandidat(sb.length() > 0 ? sb.toString() : "Demande de négociation");
            negociationRepo.save(n);

            p.setStatut(statutRepo.findByLibelle("EN_NEGOCIATION"));
            propositionRepo.save(p);
            ajouterHistorique(p, "EN_NEGOCIATION", null,
                    negociation ? "Le candidat demande une négociation" : "Réponses NON sur certaines questions");
        }

        return p;
    }

    @Transactional
    public void validerParRh(int idProposition, boolean accepte, String reponseRh, int idUtilisateur) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition).orElseThrow();

        if (!"EN_NEGOCIATION".equals(p.getStatut().getLibelle())
                && !"EN_VALIDATION_RH".equals(p.getStatut().getLibelle())) {
            throw new Exception("Cette proposition n'est pas en attente de validation RH.");
        }

        if (accepte) {
            p.setStatut(statutRepo.findByLibelle("ACCEPTEE"));
            ajouterHistorique(p, "ACCEPTEE", idUtilisateur, reponseRh);
        } else {
            p.setStatut(statutRepo.findByLibelle("REFUSEE"));
            ajouterHistorique(p, "REFUSEE", idUtilisateur, reponseRh);
        }
        propositionRepo.save(p);

        List<PropositionEmbaucheNegociation> negs = p.getNegociations();
        if (negs != null && !negs.isEmpty()) {
            PropositionEmbaucheNegociation derniere = negs.get(negs.size() - 1);
            derniere.setAccepte(accepte);
            derniere.setReponseRh(reponseRh);
            negociationRepo.save(derniere);
        }
    }

    @Transactional
    public void abandonnerCandidat(int idProposition) throws Exception {
        PropositionEmbauche p = propositionRepo.findById(idProposition).orElseThrow();
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