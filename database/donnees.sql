TRUNCATE TABLE historique_demande_status, candidat, detail_demande, demande, utilisateur, employer, poste, departement, status, diplome RESTART IDENTITY CASCADE;

INSERT INTO departement (libelle, sigle) VALUES
('Direction générale', 'DG'),
('Ressources humaines', 'RH'),
('Informatique', 'IT'),
('Commercial', 'COM');

INSERT INTO poste (id_departement, libelle, sigle, salaire) VALUES
(1, 'Directeur général', 'DG', 10000000.00),
(2, 'Responsable RH', 'RRH', 5000000.00),
(3, 'Manager Informatique', 'MI', 4500000.00),
(3, 'Chef de projet', 'CP', 3500000.00),
(3, 'Développeur Senior', 'DVS', 3000000.00),
(3, 'Développeur Junior', 'DJS', 1200000.00),
(4, 'Manager Commercial', 'MC', 4200000.00),
(4, 'Responsable de vente', 'RV', 2800000.00);

INSERT INTO employer (matricule, nom, prenom, telephone, compte, id_poste, salaire) VALUES
('EMP001', 'Maminirina', 'Michael', '0345618902', 'CP001', 1, 10000000.00),
('EMP002', 'Rakotoarisoa', 'Koloina', '0325315907', 'CP002', 2, 5500000.00),
('EMP003', 'Rajaonarivony', 'Paul', '0375402134', 'CP003', 3, 4100000.00),
('EMP004', 'Rakotoarinelina', 'Fidele', '0338712646', 'CP004', 4, 3500000.00),
('EMP005', 'Raminiarisoa', 'Noblette', '0381236800', 'CP005', 5, 3000000.00),
('EMP006', 'Rasoamiaramanana', 'Herinjatovo', '0321543698', 'CP006', 6, 1200000.00),
('EMP007', 'Raveloson', 'Andry', '0347658100', 'CP007', 7, 4200000.00),
('EMP008', 'Rafidison', 'Tendry', '0381945643', 'CP008', 8, 2800000.00);

INSERT INTO utilisateur (id_employer, email, mdp) VALUES
(1, 'dg@gmail.com', crypt('dg', gen_salt('bf', 10))),
(2, 'rh@gmail.com', crypt('rh', gen_salt('bf', 10))),
(3, 'manager@gmail.com', crypt('manager', gen_salt('bf', 10)));

INSERT INTO utilisateur (email, mdp) VALUES
('candidat@gmail.com', crypt('candidat', gen_salt('bf', 10)));

INSERT INTO status (libelle, sigle) VALUES
('Creer', 'CR'),
('Accepter', 'AC'),
('Valider', 'AU'),
('Refuser', 'RF'),
('A Revoir', 'AR'),
('Annuler', 'AN');

-- INSERT INTO demande (matricule, id_employer, date_limite) VALUES
-- ('DM007', 3, '2026-12-15');

INSERT INTO demande (matricule, id_employer, date_limite) VALUES
('DM001', 3, '2026-11-15'),
('DM002', 3, '2026-11-25'),
('DM003', 3, '2026-11-30'),
('DM004', 3, '2026-12-05'),
('DM005', 3, '2026-12-10'),
('DM006', 3, '2026-12-15');

INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date) VALUES
(1, 1, 3, '2026-10-18'),
(2, 1, 3, '2026-10-18'),
(2, 2, 2, '2026-10-19'),
(3, 1, 3, '2026-10-20'),
(3, 2, 2, '2026-10-21'),
(3, 3, 1, '2026-10-22'),
(4, 1, 3, '2026-10-18'),
(4, 4, 2, '2026-10-23'),
(5, 1, 3, '2026-10-18'),
(5, 5, 2, '2026-10-24'),
(6, 1, 3, '2026-10-18'),
(6, 6, 3, '2026-10-25');

INSERT INTO diplome (annee_etude, nom_diplome) VALUES
(3, 'Licence'),
(5, 'Master'),
(8, 'Doctorat');

-- INSERT INTO detail_demande (id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience) VALUES
-- (7, 7, 4, 2, 'Demande 6 test ', 1, 2);

INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date) VALUES
(7, 2, 3, '2026-10-29');

INSERT INTO detail_demande (id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience) VALUES
(1, 4, 3, 2, 'Projet Atlas', 2, 3),
(1, 5, 3, 3, 'Projet Atlas', 2, 3),
(2, 4, 3, 1, 'Projet Akoho', 2, 3),
(2, 6, 3, 4, 'Projet Akoho', 2, 1),
(3, 5, 3, 3, 'Création de SaaS', 1, 2),
(3, 6, 3, 4, 'Création de SaaS', 1, 2),
(4, 4, 4, 2, 'Création d''application mobile', 1, 3),
(4, 6, 4, 3, 'Création d''application mobile', 1, 1),
(5, 5, 4, 2, 'Création d''une application de gestion', 2, 5),
(5, 4, 4, 4, 'Création d''une application de gestion', 2, 5),
(6, 6, 4, 1, 'Refonte de site web', 1, 1),
(6, 5, 4, 4, 'Refonte de site web', 1, 2);

INSERT INTO candidat (id_utilisateur, id_detail_demande, cv_nom, lm_nom, status, date_postulation, prenom, nom, email, telephone, titre, resume, dernier_poste, entreprise, id_diplome, annee_experience_prof, ecole)
VALUES
(4, 3, 'cv_candidat_1.pdf', 'lm_candidat_1.pdf', 'En attente', '2026-10-22', 'Alice', 'Rabe', 'candidat@gmail.com', '0341234567', 'Développeuse Java', 'Développeuse backend avec 3 ans d''expérience en Java Spring.', 'Développeuse Java', 'MadaTech', 2, 3, 'Université d''Antananarivo');