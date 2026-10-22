-- ============================================================================
--  Gestion-RH — donnees de reference et jeu de demonstration
--
--  A executer apres database/table.sql :
--      psql -U postgres -d gestion_entreprise -f database/donnees.sql
--
--  Le script est rejouable : il reinitialise les tables de donnees avant
--  insertion, ce qui garantit des identifiants stables pour la demonstration.
-- ============================================================================

TRUNCATE TABLE demande_a_revoir,
               historique_demande_status,
               detail_demande,
               demande,
               grille_budget,
               historique_budget_annuel,
               utilisateur,
               employer,
               poste,
               departement,
               diplome,
               status,
               roles
    RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------------------
--  1. Roles de l'application
-- ---------------------------------------------------------------------------
INSERT INTO roles (id, roles) VALUES
    (1, 'RH'),
    (2, 'Manager'),
    (3, 'Finance'),
    (4, 'DG');

-- ---------------------------------------------------------------------------
--  2. Referentiel
-- ---------------------------------------------------------------------------
INSERT INTO departement (id, libelle, sigle) VALUES
    (1, 'Direction Generale', 'DG'),
    (2, 'Ressources humaine', 'RH'),
    (3, 'Informatique', 'IT'),
    (4, 'Commercial', 'CM'),
    (5, 'Finance', 'FI');

INSERT INTO poste (id, id_departement, libelle, sigle, salaire) VALUES
    (1, 1, 'Directeur Generale',   'DG',  10000000),
    (2, 2, 'Responsable RH',       'RRH',  5000000),
    (3, 3, 'Manager Informatique', 'MI',   4000000),
    (4, 3, 'Chef de projet',       'CP',   3500000),
    (5, 3, 'Dev Senior',           'DVS',  3000000),
    (6, 3, 'Dev Junior',           'DJS',  1000000),
    (7, 4, 'Manager Commerciale',  'MC',   4000000),
    (8, 4, 'Responsable de vente', 'RV',   3000000),
    (9, 5, 'Responsable Finance',  'RF',   4500000);

INSERT INTO employer (id, matricule, nom, prenom, telephone, compte, id_poste, salaire) VALUES
    (1, 'EMP001', 'Maminirina',        'Michael',     '0345618902', 'CP001', 1, 10000000),
    (2, 'EMP002', 'Rakotoarisoa',      'Koloina',     '0325315907', 'CP002', 2,  5500000),
    (3, 'EMP003', 'Rajaonarivony',     'Paul',        '0375402134', 'CP003', 3,  4100000),
    (4, 'EMP004', 'Rakotoarinelina',   'Fidele',      '0338712646', 'CP004', 4, 35000000),
    (5, 'EMP005', 'Raminiarisoa',      'Noblette',    '03812368',   'CP005', 5,  3000000),
    (6, 'EMP006', 'Rasoamiaramanana',  'Herinjatovo', '0321543698', 'CP006', 6,  1000000),
    (7, 'EMP007', 'Raveloson',         'Andry',       '03476581',   'CP007', 7,  4000000),
    (8, 'EMP008', 'Rafidison',         'Tendry',      '0381945643', 'CP008', 8,  3000000),
    (9, 'EMP009', 'Rasoanaivo',        'Finora',      '0331122544', 'CP009', 9,  4500000);

-- Mots de passe : dg / rh / manager / finance / candidat
INSERT INTO utilisateur (id, id_employer, email, mdp, id_role) VALUES
    (1, 1, 'dg@gmail.com',         crypt('dg',      gen_salt('bf', 10)), 4),
    (2, 2, 'rh@gmail.com',         crypt('rh',      gen_salt('bf', 10)), 1),
    (3, 3, 'manager@gmail.com',    crypt('manager', gen_salt('bf', 10)), 2),
    (4, 9, 'finance@gmail.com',    crypt('finance', gen_salt('bf', 10)), 3),
    (5, NULL, 'candidat@gmail.com', crypt('candidat',gen_salt('bf', 10)), NULL);

INSERT INTO diplome (id, annee_etude, nom_diplome) VALUES
    (1, 3, 'Licence'),
    (2, 5, 'Master'),
    (3, 8, 'Doctorat');

-- ---------------------------------------------------------------------------
--  3. Statuts du circuit de validation
-- ---------------------------------------------------------------------------
INSERT INTO status (id, libelle, sigle) VALUES
    (1, 'cree',           'CREE'),
    (2, 'valider rh',     'VALIDER_RH'),
    (3, 'valider finance', 'VALIDER_FINANCE'),
    (4, 'valider dg',     'VALIDER_DG'),
    (5, 'a revoir',      'A_REVOIR'),
    (6, 'refuser',       'REFUSER'),
    (7, 'abandone',      'ABANDONNE'),
    (8, 'expire',        'EXPIRE');

-- ---------------------------------------------------------------------------
--  4. Budgets
-- ---------------------------------------------------------------------------
-- Grille budgetaire par poste : la ligne la plus recente <= aujourd'hui fait foi.
INSERT INTO grille_budget (id, id_poste, budget_min, budget_max, date_effective) VALUES
    (1, 3, 2000000, 2800000, '2026-01-01'),
    (2, 4, 2500000, 3500000, '2026-01-01'),
    (3, 4, 2800000, 3800000, '2026-07-01'),
    (4, 5, 1800000, 2500000, '2026-01-01'),
    (5, 6,  700000, 1200000, '2026-01-01'),
    (6, 7, 2000000, 2800000, '2026-01-01'),
    (7, 8, 1500000, 2200000, '2026-01-01');

-- Budget global annuel (le plus recent <= aujourd'hui fait foi).
INSERT INTO historique_budget_annuel (id, valeur, date_effective) VALUES
    (1, 45000000, '2025-01-01'),
    (2, 20000000, '2026-01-01');

-- ---------------------------------------------------------------------------
--  5. Demandes
-- ---------------------------------------------------------------------------
INSERT INTO demande (id, matricule, id_employer, date_limite) VALUES
    (1, 'DM001', 3, '2026-11-23'),
    (2, 'DM002', 3, '2026-11-30'),
    (3, 'DM003', 3, '2026-11-30'),
    (4, 'DM004', 3, '2026-11-30'),
    (5, 'DM005', 3, '2026-11-30'),
    (6, 'DM006', 3, '2026-12-15'),
    (7, 'DM007', 3, '2026-10-01'),
    (8, 'DM008', 3, '2026-11-30'),
    (9, 'DM009', 3, '2026-12-20');

-- ---------------------------------------------------------------------------
--  6. Circuit de validation : une ligne par action realisee
-- ---------------------------------------------------------------------------
INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date, motif) VALUES
    -- DM001 : creee, en attente de la RH
    (1, 1, 3, '2026-10-21', NULL),
    -- DM002 : validee par la RH, en attente de la Finance
    (2, 1, 3, '2026-10-21', NULL),
    (2, 2, 2, '2026-10-22', NULL),
    -- DM003 : validee par la Finance, en attente de la DG
    (3, 1, 3, '2026-10-21', NULL),
    (3, 2, 2, '2026-10-22', NULL),
    (3, 3, 9, '2026-10-23', NULL),
    -- DM004 : validee par la DG, terminee
    (4, 1, 3, '2026-10-21', NULL),
    (4, 2, 2, '2026-10-22', NULL),
    (4, 3, 9, '2026-10-23', NULL),
    (4, 4, 1, '2026-10-24', NULL),
    -- DM005 : revisee par la RH, en attente du manager
    (5, 1, 3, '2026-10-21', NULL),
    (5, 5, 2, '2026-10-26', 'Preciser le profil recherche et l''experience attendue'),
    -- DM006 : revisee par la Finance, en attente du manager
    (6, 1, 3, '2026-10-21', NULL),
    (6, 2, 2, '2026-10-22', NULL),
    (6, 5, 9, '2026-10-27', 'Le profil du poste Dev Senior ne correspond pas a la grille'),
    -- DM007 : expiree (date limite depassee)
    (7, 1, 3, '2026-10-01', NULL),
    (7, 8, NULL, '2026-10-02', 'Date limite depassee'),
    -- DM008 : refusee par la RH
    (8, 1, 3, '2026-10-21', NULL),
    (8, 6, 2, '2026-10-25', 'Poste non pourvu dans la grille budgetaire'),
    -- DM009 : validee par la RH, depasse le budget annuel (cotation Finance bloquee)
    (9, 1, 3, '2026-10-21', NULL),
    (9, 2, 2, '2026-10-22', NULL);

-- ---------------------------------------------------------------------------
--  7. Lignes de demande
-- ---------------------------------------------------------------------------
INSERT INTO detail_demande (id, id_demande, id_poste, id_departement, nbr_personnel, description, id_diplome, annee_experience) VALUES
    (1,  1, 4, 3, 2, 'Projet Atlas',                       2, 3),
    (2,  1, 5, 3, 3, 'Projet Atlas',                       2, 3),
    (3,  2, 6, 3, 4, 'Projet Akoho',                       2, 1),
    (4,  2, 4, 3, 1, 'Projet Akoho',                       2, 3),
    (5,  3, 5, 3, 3, 'Creation de Saas',                   1, 2),
    (6,  3, 6, 3, 4, 'Creation de Saas',                   1, 2),
    (7,  4, 4, 4, 2, 'Refonte du site vitrine',             1, 3),
    (8,  4, 6, 4, 3, 'Refonte du site vitrine',             1, 1),
    (9,  5, 5, 4, 2, 'Application de gestion',              2, 5),
    (10, 5, 4, 4, 4, 'Application de gestion',              2, 5),
    (11, 6, 6, 4, 1, 'Refonte de site web',                 1, 1),
    (12, 6, 5, 4, 4, 'Refonte de site web',                 1, 2),
    (13, 7, 3, 3, 1, 'Sprint de securisation',              2, 4),
    (14, 8, 8, 4, 2, 'Ouverture agence Nord',               1, 2),
    (15, 9, 4, 4, 30, 'Centre de services',                 2, 5);

-- ---------------------------------------------------------------------------
--  8. Revisions demandees (RH / Finance / DG -> manager)
-- ---------------------------------------------------------------------------
INSERT INTO demande_a_revoir (id, id_demandeur, id_demande_details, date, commentaire, nbr_personnel_propose) VALUES
    (1, 2,  9, '2026-10-26', 'Preciser le profil recherche et l''experience attendue', 3),
    (2, 2, 10, '2026-10-26', 'Preciser le profil recherche et l''experience attendue', 2),
    (3, 9, 11, '2026-10-27', 'Le profil du poste Dev Senior ne correspond pas a la grille', 1),
    (4, 9, 12, '2026-10-27', 'Le profil du poste Dev Senior ne correspond pas a la grille', 2);

-- ---------------------------------------------------------------------------
--  9. Sequences
--
--  Les identifiants ci-dessus sont explicites : les sequences doivent etre
--  repositionnees, sinon la premiere creation d'une ligne depuis l'application
--  echoue sur une cle deja utilisee.
-- ---------------------------------------------------------------------------
SELECT setval('roles_id_seq',                     (SELECT MAX(id) FROM roles));
SELECT setval('departement_id_seq',               (SELECT MAX(id) FROM departement));
SELECT setval('poste_id_seq',                     (SELECT MAX(id) FROM poste));
SELECT setval('employer_id_seq',                  (SELECT MAX(id) FROM employer));
SELECT setval('utilisateur_id_seq',               (SELECT MAX(id) FROM utilisateur));
SELECT setval('diplome_id_seq',                   (SELECT MAX(id) FROM diplome));
SELECT setval('status_id_seq',                    (SELECT MAX(id) FROM status));
SELECT setval('grille_budget_id_seq',             (SELECT MAX(id) FROM grille_budget));
SELECT setval('historique_budget_annuel_id_seq', (SELECT MAX(id) FROM historique_budget_annuel));
SELECT setval('demande_id_seq',                   (SELECT MAX(id) FROM demande));
SELECT setval('historique_demande_status_id_seq', (SELECT MAX(id) FROM historique_demande_status));
SELECT setval('detail_demande_id_seq',            (SELECT MAX(id) FROM detail_demande));
SELECT setval('demande_a_revoir_id_seq',          (SELECT MAX(id) FROM demande_a_revoir));
