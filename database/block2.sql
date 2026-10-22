-- =============================================================
--  BLOC 2 : Tests techniques
-- =============================================================

-- Historique des changements de statut de candidature
CREATE TABLE historique_candidat_statut (
    id SERIAL PRIMARY KEY,
    id_candidat INT NOT NULL,
    ancien_statut VARCHAR(50),
    nouveau_statut VARCHAR(50) NOT NULL,
    motif TEXT,
    id_utilisateur INT NOT NULL,
    date_changement TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_candidat) REFERENCES candidat(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id)
);

-- Niveaux de test (référence)
CREATE TABLE test_technique_niveau (
    id SERIAL PRIMARY KEY,
    niveau INT NOT NULL UNIQUE,
    libelle VARCHAR(50) NOT NULL
);

INSERT INTO test_technique_niveau (niveau, libelle) VALUES
(1, 'Test écrit'),
(2, 'Niveau 2'),
(3, 'Niveau 3');

-- Un test par (poste, niveau)
CREATE TABLE test_technique (
    id SERIAL PRIMARY KEY,
    id_poste INT NOT NULL,
    id_test_technique_niveau INT NOT NULL,
    seuil_reussite INT NOT NULL DEFAULT 60,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_test_technique_niveau) REFERENCES test_technique_niveau(id),
    CHECK (seuil_reussite BETWEEN 0 AND 100)
);

CREATE UNIQUE INDEX uk_test_poste_niveau_actif
    ON test_technique(id_poste, id_test_technique_niveau)
    WHERE actif;

-- Questions QCM
CREATE TABLE test_technique_question (
    id SERIAL PRIMARY KEY,
    id_test_technique INT NOT NULL,
    ordre INT NOT NULL,
    enonce TEXT NOT NULL,
    choix_a TEXT NOT NULL,
    choix_b TEXT NOT NULL,
    choix_c TEXT NOT NULL,
    choix_d TEXT NOT NULL,
    reponse_correcte CHAR(1) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (id_test_technique) REFERENCES test_technique(id),
    CHECK (reponse_correcte IN ('A','B','C','D'))
);

-- Passage d'un test par un candidat
CREATE TABLE test_technique_passage (
    id SERIAL PRIMARY KEY,
    id_candidat INT NOT NULL,
    id_test_technique INT NOT NULL,
    score INT,
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    date_debut TIMESTAMP NOT NULL DEFAULT NOW(),
    date_fin TIMESTAMP,
    FOREIGN KEY (id_candidat) REFERENCES candidat(id),
    FOREIGN KEY (id_test_technique) REFERENCES test_technique(id),
    CHECK (statut IN ('EN_COURS','VALIDE','ECHOUE'))
);

-- Réponses du candidat
CREATE TABLE test_technique_reponse (
    id SERIAL PRIMARY KEY,
    id_test_technique_passage INT NOT NULL,
    id_test_technique_question INT NOT NULL,
    reponse_choisie CHAR(1) NOT NULL,
    correcte BOOLEAN NOT NULL,
    FOREIGN KEY (id_test_technique_passage) REFERENCES test_technique_passage(id),
    FOREIGN KEY (id_test_technique_question) REFERENCES test_technique_question(id),
    CHECK (reponse_choisie IN ('A','B','C','D'))
);

-- =============================================================
--  SEED : 3 tests par poste IT (postes 5, 6, 7)
--  + 3 questions par niveau
-- =============================================================

INSERT INTO test_technique (id_poste, id_test_technique_niveau, seuil_reussite) VALUES
(5, 1, 60), (5, 2, 60), (5, 3, 60),
(6, 1, 60), (6, 2, 60), (6, 3, 60),
(7, 1, 60), (7, 2, 60), (7, 3, 60);

-- ---------- Poste 5 : Chef de projet ----------
-- L1 (test id=1)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(1, 1, 'Qu''est-ce qu''un sprint en Scrum ?', 'Une réunion', 'Un cycle de développement de 1 à 4 semaines', 'Un document', 'Un rôle', 'B'),
(1, 2, 'Que signifie MVP ?', 'Most Valuable Player', 'Minimum Viable Product', 'Multi Variable Process', 'Mission Very Possible', 'B'),
(1, 3, 'Quel est le rôle du Product Owner ?', 'Coder', 'Tester', 'Définir et prioriser le backlog', 'Gérer l''infrastructure', 'C');

-- L2 (test id=2)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(2, 1, 'Qu''est-ce qu''une user story ?', 'Une histoire utilisateur', 'Une exigence fonctionnelle du point de vue utilisateur', 'Un bug', 'Un document technique', 'B'),
(2, 2, 'Quel indicateur mesure la vélocité ?', 'Nombre de bugs', 'Story points livrés par sprint', 'Lignes de code', 'Nombre de réunions', 'B'),
(2, 3, 'Que faire si un sprint échoue ?', 'Blâmer l''équipe', 'Faire une rétrospective et identifier les causes', 'Ignorer', 'Annuler le projet', 'B');

-- L3 (test id=3)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(3, 1, 'Qu''est-ce que le SAFe ?', 'Un framework Agile à l''échelle', 'Un langage', 'Un outil', 'Une méthode Waterfall', 'A'),
(3, 2, 'Comment gérer les dépendances entre équipes ?', 'Ignorer', 'Planifier des sync réguliers et un PI Planning', 'Développer en silo', 'Changer d''équipe', 'B'),
(3, 3, 'Qu''est-ce que le cycle PDCA ?', 'Plan-Do-Check-Act', 'Plan-Design-Code-Apply', 'Prepare-Develop-Check-Agree', 'None of the above', 'A');

-- ---------- Poste 6 : Dev Senior ----------
-- L1 (test id=4)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(4, 1, 'Qu''est-ce que la POO ?', 'Programmation Orientée Objet', 'Procédure Orientée Ordinateur', 'Pattern Objet Ouvert', 'Aucune', 'A'),
(4, 2, 'Quel principe SOLID concerne les responsabilités ?', 'S (Single Responsibility)', 'O (Open/Closed)', 'L (Liskov)', 'I (Interface Segregation)', 'A'),
(4, 3, 'Que fait le mot-clé final en Java ?', 'Rend une classe immuable', 'Empêche l''héritage/la réassignation', 'Supprime une variable', 'Ferme un fichier', 'B');

-- L2 (test id=5)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(5, 1, 'Qu''est-ce qu''une transaction ACID ?', 'Un test', 'Un ensemble d''opérations atomiques et cohérentes', 'Un algorithme', 'Un compilateur', 'B'),
(5, 2, 'Quel pattern permet d''inverser les dépendances ?', 'Singleton', 'Dependency Injection', 'Observer', 'Adapter', 'B'),
(5, 3, 'Qu''est-ce qu''une race condition ?', 'Un bug lié à la concurrence', 'Un compilateur', 'Un algorithme', 'Un protocole', 'A');

-- L3 (test id=6)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(6, 1, 'Qu''est-ce que le pattern Saga ?', 'Un pattern de gestion de transactions distribuées', 'Un framework UI', 'Un type de base de données', 'Un protocole réseau', 'A'),
(6, 2, 'Quel pattern évite les requêtes N+1 ?', 'Eager fetching / JOIN FETCH', 'Lazy loading systématique', 'SELECT *', 'Aucun', 'A'),
(6, 3, 'Qu''est-ce que la CAP theorem ?', 'Un théorème sur cohérence, disponibilité et partition', 'Un test unitaire', 'Un pattern', 'Un compilateur', 'A');

-- ---------- Poste 7 : Dev Junior ----------
-- L1 (test id=7)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(7, 1, 'Que signifie HTML ?', 'HyperText Markup Language', 'High Tech Modern Language', 'Home Tool Markup Language', 'Hyper Transfer Memory Link', 'A'),
(7, 2, 'Que fait une boucle for ?', 'Répéter un bloc', 'Déclarer une variable', 'Créer une fonction', 'Fermer un fichier', 'A'),
(7, 3, 'Qu''est-ce qu''une variable ?', 'Un espace mémoire nommé', 'Un fichier', 'Un type de base de données', 'Un langage', 'A');

-- L2 (test id=8)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(8, 1, 'Qu''est-ce que Git ?', 'Un gestionnaire de versions', 'Un langage', 'Un serveur web', 'Un compilateur', 'A'),
(8, 2, 'Que fait un JOIN SQL ?', 'Fusionner des tables', 'Supprimer une table', 'Créer un index', 'Aucune', 'A'),
(8, 3, 'Qu''est-ce qu''une API REST ?', 'Une interface HTTP pour échanger des données', 'Un langage', 'Un protocole réseau', 'Un framework UI', 'A');

-- L3 (test id=9)
INSERT INTO test_technique_question (id_test_technique, ordre, enonce, choix_a, choix_b, choix_c, choix_d, reponse_correcte) VALUES
(9, 1, 'Qu''est-ce que Docker ?', 'Un outil de conteneurisation', 'Un langage', 'Un serveur', 'Un compilateur', 'A'),
(9, 2, 'Qu''est-ce qu''un test unitaire ?', 'Un test d''une unité isolée du code', 'Un test d''intégration', 'Un test manuel', 'Aucune', 'A'),
(9, 3, 'Que fait une CI/CD ?', 'Automatise les tests et déploiements', 'Gère les bases de données', 'Compile uniquement', 'Aucune', 'A');


UPDATE candidat
SET status = 'EN_ATTENTE'
WHERE status IN ('En attente', 'EN ATTENTE', 'en_attente', 'En_Attente');
UPDATE candidat SET status = 'EN_TEST'     WHERE status IN ('En test', 'EN TEST');
UPDATE candidat SET status = 'TESTS_REUSSIS' WHERE status IN ('Tests réussis', 'TESTS REUSSIS');
UPDATE candidat SET status = 'TEST_ECHOUE' WHERE status IN ('Test échoué', 'TEST ECHOUE');
UPDATE candidat SET status = 'EN_ENTRETIEN' WHERE status IN ('En entretien', 'EN ENTRETIEN');
UPDATE candidat SET status = 'ACCEPTE'     WHERE status IN ('Accepté', 'ACCEPTÉ', 'accepte');
UPDATE candidat SET status = 'REFUSE'      WHERE status IN ('Refusé', 'REFUSÉ', 'refuse');