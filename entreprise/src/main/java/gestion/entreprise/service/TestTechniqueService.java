package gestion.entreprise.service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TestTechniqueService {

    @Autowired private TestTechniqueRepository testRepository;
    @Autowired private TestTechniqueQuestionRepository questionRepository;
    @Autowired private TestTechniquePassageRepository passageRepository;
    @Autowired private CandidatRepository candidatRepository;
    @Autowired private CandidatureWorkflowService workflowService;

    /** Niveau max validé pour un candidat (0 si aucun test validé). */
    public int getNiveauMaxValide(Candidat candidat) {
        List<TestTechniquePassage> passages = passageRepository
                .findByCandidatIdOrderByDateDebut(candidat.getId());
        int max = 0;
        for (TestTechniquePassage p : passages) {
            if ("VALIDE".equals(p.getStatut())) {
                int n = p.getTestTechnique().getNiveau().getNiveau();
                if (n > max) max = n;
            }
        }
        return max;
    }

    /** Prochain niveau à passer (1, 2, 3, ou 0 si terminé/échoué). */
    public int getProchainNiveau(Candidat candidat) {
        int max = getNiveauMaxValide(candidat);
        if (max >= 3) return 0;

        // Si le niveau max+1 a déjà été échoué, on bloque
        List<TestTechniquePassage> passages = passageRepository
                .findByCandidatIdOrderByDateDebut(candidat.getId());
        for (TestTechniquePassage p : passages) {
            if ("ECHOUE".equals(p.getStatut())
                    && p.getTestTechnique().getNiveau().getNiveau() == max + 1) {
                return 0;
            }
        }
        return max + 1;
    }

    /** Récupère le test actif pour le poste du candidat et le niveau demandé. */
    public TestTechnique getTest(int idCandidat, int niveau) throws Exception {
        Candidat candidat = candidatRepository.findById(idCandidat)
                .orElseThrow(() -> new Exception("Candidature introuvable"));
        if (candidat.getDetailDemande() == null) throw new Exception("Aucun poste associé");

        int idPoste = candidat.getDetailDemande().getPoste().getId();
        TestTechnique test = testRepository.findActifByPosteAndNiveau(idPoste, niveau);
        if (test == null) throw new Exception("Aucun test configuré pour ce poste et ce niveau");
        return test;
    }

    /**
     * Soumet les réponses du candidat et calcule le score.
     * Les réponses sont persistées en cascade via passage.getReponses().
     *
     * @param reponses map idQuestion -> 'A'/'B'/'C'/'D'
     */
    @Transactional
    public TestTechniquePassage soumettre(int idCandidat, int niveau,
                                          Map<Integer, String> reponses,
                                          int idUtilisateur) throws Exception {

        Candidat candidat = candidatRepository.findById(idCandidat)
                .orElseThrow(() -> new Exception("Candidature introuvable"));

        int prochain = getProchainNiveau(candidat);
        if (prochain != niveau) {
            throw new Exception("Ce niveau n'est pas accessible.");
        }

        TestTechnique test = getTest(idCandidat, niveau);
        List<TestTechniqueQuestion> questions = questionRepository
                .findByTestTechniqueIdAndActifTrueOrderByOrdre(test.getId());

        if (questions.isEmpty()) throw new Exception("Aucune question pour ce test.");

        // 1. Créer le passage
        TestTechniquePassage passage = new TestTechniquePassage();
        passage.setCandidat(candidat);
        passage.setTestTechnique(test);
        passage.setStatut("EN_COURS");

        // 2. Construire les réponses
        int bonnes = 0;
        for (TestTechniqueQuestion q : questions) {
            String choix = reponses.get(q.getId());
            boolean correct = choix != null && choix.equalsIgnoreCase(q.getReponseCorrecte());
            if (correct) bonnes++;

            TestTechniqueReponse rep = new TestTechniqueReponse();
            rep.setQuestion(q);
            rep.setReponseChoisie(choix == null ? "A" : choix.toUpperCase());
            rep.setCorrecte(correct);

            passage.addReponse(rep);   // ✅ lien bidirectionnel + ajout à la collection
        }

        // 3. Calculer le score AVANT le save
        int score = (bonnes * 100) / questions.size();
        passage.setScore(score);
        passage.setStatut(score >= test.getSeuilReussite() ? "VALIDE" : "ECHOUE");
        passage.setDateFin(LocalDateTime.now());

        // 4. UN SEUL save → cascade INSERT passage + toutes les réponses
        passage = passageRepository.save(passage);

        // 5. Workflow statut candidat
        if ("VALIDE".equals(passage.getStatut())) {
            if (niveau == 3) {
                workflowService.changerStatut(candidat, "TESTS_REUSSIS", idUtilisateur,
                        "Tous les tests techniques réussis");
            } else if (niveau == 1) {
                workflowService.changerStatut(candidat, "EN_TEST", idUtilisateur,
                        "Test niveau 1 réussi");
            }
        } else {
            workflowService.changerStatut(candidat, "TEST_ECHOUE", idUtilisateur,
                    "Test niveau " + niveau + " échoué (" + score + "%)");
        }

        return passage;
    }

    public List<TestTechniquePassage> getPassages(int idCandidat) {
        return passageRepository.findByCandidatIdOrderByDateDebut(idCandidat);
    }
}