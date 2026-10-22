INSERT INTO departement(libelle, sigle) VALUES
('Direction General','DG'),
('Ressources humaine','RH'),
('Informatique','IT'),
('Commercial','CM');

INSERT INTO poste(id_departement, libelle, sigle, salaire) VALUES
(1,'Directeur Generale','DG',10000000),
(2,'Responsable RH','RRH',5000000),
(3,'Manager Informatique','MI',4000000),
(3,'Chef de projet','CP',3500000),
(3,'Dev Senior','DVS',3000000),
(3,'Dev Junior','DJS',1000000),
(4,'Manager Commerciale','MC',4000000),
(4,'Responsable de vente','RV',3000000);

INSERT INTO employer (matricule, nom, prenom, telephone, compte, id_poste, salaire) VALUES
('EMP001','Maminirina','Michael','0345618902','CP001',1,10000000),
('EMP002','Rakotoarisoa','Koloina','0325315907','CP002',2,5500000),
('EMP003','Rajaonarivony','Paul','0375402134','CP003',3,4100000),
('EMP004','Rakotoarinelina','Fidele','0338712646','CP004',4,35000000),
('EMP005','Raminiarisoa','Noblette','03812368','CP005',5,3000000),
('EMP006','Rasoamiaramanana','Herinjatovo','0321543698','CP006',6,1000000),
('EMP007','Raveloson','Andry','03476581','CP007',7,4000000),
('EMP008','Rafidison','Tendry','0381945643','CP008',8,3000000);

INSERT INTO utilisateur(id_employer, email, mdp) VALUES
(1,'dg@gmail.com', crypt('dg', gen_salt('bf', 10))),
(2,'rh@gmail.com', crypt('rh', gen_salt('bf', 10))),
(3,'manager@gmail.com', crypt('manager', gen_salt('bf', 10)));

INSERT INTO utilisateur(email, mdp) VALUES
('candidat@gmail.com', crypt('candidat', gen_salt('bf', 10)));

INSERT INTO status(libelle, sigle) VALUES
('Creer','CR'),
('Accepter','AC'),  -- Pour RH
('Valider','AU'),   -- Pour DG
('Refuser','RF'),
('A Revoir','AR'),
('Annuler','AN');

INSERT INTO demande(matricule, id_employer, date_limite) VALUES
('DM001',3,'2026-11-23'),  -- Creer
('DM002',3,'2026-11-30'),  -- Accepter par la RH
('DM003',3,'2026-11-30'),  -- Valider par le DG
('DM004',3,'2026-11-30'),  -- Refuser
('DM005',3,'2026-11-30'),  -- A Revoir
('DM006',3,'2026-11-30');  -- Annuler

INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date) VALUES
(1,1,3,'2026-10-21'),
(2,1,3,'2026-10-21'),
(2,2,2,'2026-10-22'),
(3,1,3,'2026-10-21'),
(3,2,2,'2026-10-23'),
(3,3,1,'2026-10-24'),
(4,1,3,'2026-10-21'),
(4,4,2,'2026-10-25'),
(5,1,3,'2026-10-21'),
(5,5,2,'2026-10-26'),
(6,1,3,'2026-10-21'),
(6,6,3,'2026-10-27');

INSERT INTO diplome(annee_etude, nom_diplome) VALUES
(3,'Licence'),
(5,'Master'),
(8,'Doctorat');

INSERT INTO detail_demande(id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience) VALUES
(1,4,3,2,'Projet Atlas',2,3),
(1,5,3,3,'Projet Atlas',2,3),
(2,6,3,4,'Projet Akoho',2,1),
(2,4,3,1,'Projet Akoho',2,3),
(3,5,3,3,'Creation de Saas',1,2),
(3,6,3,4,'creation de Saas',1,2),
(4,4,4,2,'Creation d''application mobile',1,3),
(4,6,4,3,'Creation d''application mobile',1,1),
(5,5,4,2,'Creation d''une application de gestion',2,5),
(5,4,4,4,'Creation d''une application de gestion',2,5),
(6,6,4,1,'Refonte de site web',1,1),
(6,5,4,4,'Refonte de site web',1,2);