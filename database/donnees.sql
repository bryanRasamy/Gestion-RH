-- Roles
INSERT INTO role(libelle, sigle) VALUES
('Manager', 'MANAGER'),
('Ressources Humaines', 'RH'),
('Finances', 'FINANCE'),
('Directeur General', 'DG');

-- Departements
INSERT INTO departement(libelle, sigle) VALUES
('Direction General','DG'),
('Ressources humaine','RH'),
('Informatique','IT'),
('Commercial','CM'),
('Finance','FI');

-- Postes (sans salaire, c'est la grille qui gère)
INSERT INTO poste(id_departement, libelle, sigle) VALUES
(1,'Directeur Generale','DG'),
(2,'Responsable RH','RRH'),
(5,'Directeur Financier','DF'),
(3,'Manager Informatique','MI'),
(3,'Chef de projet','CP'),
(3,'Dev Senior', 'DVS'),
(3,'Dev Junior', 'DJS'),
(4,'Manager Commerciale','MC'),
(4,'Responsable de vente','RV');

-- Employers
INSERT INTO employer (matricule,nom,prenom,telephone,compte,id_poste,salaire) VALUES
('EMP001','Maminirina','Michael','0345618902','CP001',1,10000000),
('EMP002','Rakotoarisoa','Koloina','0325315907','CP002',2,5500000),
('EMP003','Andriamihaja','Tiana','0371234567','CP003',3,6000000),
('EMP004','Rajaonarivony','Paul','0375402134','CP004',4,4100000),
('EMP005','Rakotoarinelina','Fidele','0338712646','CP005',5,3500000),
('EMP006','Raminiarisoa','Noblette','03812368','CP006',6,3000000),
('EMP007','Rasoamiaramanana','Herinjatovo','0321543698','CP007',7,1000000),
('EMP008','Raveloson','Andry','03476581','CP008',8,4000000),
('EMP009','Rafidison','Tendry','0381945643','CP009',9,3000000);

-- Utilisateurs avec roles
INSERT INTO utilisateur(id_employer, id_role, email, mdp) VALUES
(1, 4, 'dg@gmail.com',       crypt('dg',       gen_salt('bf', 10)));
INSERT INTO utilisateur(id_employer, id_role, email, mdp) VALUES
(2, 2, 'rh@gmail.com',       crypt('rh',       gen_salt('bf', 10)));
INSERT INTO utilisateur(id_employer, id_role, email, mdp) VALUES
(3, 3, 'finance@gmail.com',  crypt('finance',  gen_salt('bf', 10)));
INSERT INTO utilisateur(id_employer, id_role, email, mdp) VALUES
(4, 1, 'manager@gmail.com',  crypt('manager',  gen_salt('bf', 10)));
INSERT INTO utilisateur(email, mdp) VALUES
('candidat@gmail.com', crypt('candidat', gen_salt('bf', 10)));

-- Statuts
INSERT INTO status(libelle, sigle) VALUES
('Creer','CR'),
('Valider','VA'),
('Refuser','RF'),
('A Revoir','AR'),
('Annuler','AN');

-- Demandes (faites par le manager EMP004, employer id=4)
INSERT INTO demande(matricule, id_employer, date_limite) VALUES
('DM001', 4, '2026-11-23 00:00:00'),
('DM002', 4, '2026-11-30 00:00:00'),
('DM003', 4, '2026-11-30 00:00:00'),
('DM004', 4, '2026-11-30 00:00:00'),
('DM005', 4, '2026-11-30 00:00:00'),
('DM006', 4, '2026-11-30 00:00:00');

-- Historique statuts (CR=1, VA=2, RF=3, AR=4, AN=5)

-- DM001: Creer par manager
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date) VALUES
(1, 1, 4, '2026-10-21 08:00:00');

-- DM002: Creer par manager, puis Valider par RH
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date) VALUES
(2, 1, 4, '2026-10-21 08:05:00'),
(2, 2, 2, '2026-10-22 09:30:00');

-- DM003: Creer, Valider par RH, Valider par Finance
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date) VALUES
(3, 1, 4, '2026-10-21 08:10:00'),
(3, 2, 2, '2026-10-22 10:00:00'),
(3, 2, 3, '2026-10-24 14:00:00');

-- DM004: Creer puis Refuser par RH
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, motif, date) VALUES
(4, 1, 4, null,                                    '2026-10-21 08:15:00'),
(4, 3, 2, 'Budget insuffisant pour ce trimestre',  '2026-10-25 11:00:00');

-- DM005: Creer puis A Revoir par RH
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, motif, date) VALUES
(5, 1, 4, null,                                              '2026-10-21 08:20:00'),
(5, 4, 2, 'Nombre de postes trop eleve, merci de revoir',   '2026-10-26 15:30:00');

-- DM006: Creer puis Annuler
INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date) VALUES
(6, 1, 4, '2026-10-21 08:25:00'),
(6, 5, 4, '2026-10-27 16:00:00');

-- Diplomes
INSERT INTO diplome(annee_etude, nom_diplome) VALUES
(3, 'Licence'),
(5, 'Master'),
(8, 'Doctorat');

-- Detail demandes
INSERT INTO detail_demande(id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience) VALUES
(1, 5, 3, 2, 'Projet Atlas', 2, 3),
(1, 6, 3, 3, 'Projet Atlas', 2, 3),
(2, 7, 3, 4, 'Projet Akoho', 2, 1),
(2, 5, 3, 1, 'Projet Akoho', 2, 3),
(3, 6, 3, 3, 'Creation de Saas', 1, 2),
(3, 7, 3, 4, 'Creation de Saas', 1, 2),
(4, 5, 3, 2, 'Creation d''application mobile', 1, 3),
(4, 7, 3, 3, 'Creation d''application mobile', 1, 1),
(5, 6, 3, 2, 'Creation d''une application de gestion', 2, 5),
(5, 5, 3, 4, 'Creation d''une application de gestion', 2, 5),
(6, 7, 3, 1, 'Refonte de site web', 1, 1),
(6, 6, 3, 4, 'Refonte de site web', 1, 2);

-- Grille budget
INSERT INTO grille_budget(id_poste, budget_min, budget_max, date_effective) VALUES
(4, 2000000, 4000000, '2026-01-01 00:00:00'),
(5, 2500000, 3500000, '2026-01-01 00:00:00'),
(6, 2000000, 3000000, '2026-01-01 00:00:00'),
(7, 800000,  1200000, '2026-01-01 00:00:00'),
(8, 3000000, 4500000, '2026-01-01 00:00:00'),
(9, 2000000, 3200000, '2026-01-01 00:00:00');

-- Historique budget annuel
INSERT INTO historique_budget_annuel(valeur, date_effective) VALUES
(500000000, '2026-01-01 00:00:00'),
(600000000, '2027-01-01 00:00:00');

-- Demandes a revoir: snapshot effectif pour DM005
INSERT INTO demande_a_revoir(id_demandeur, id_demande_details, nb_souhaite, id_date) VALUES
(2, 9,  1, '2026-10-26 15:31:00'),
(2, 10, 2, '2026-10-26 15:31:00');