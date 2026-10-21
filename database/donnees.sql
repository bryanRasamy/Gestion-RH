INSERT INTO departement(libelle, sigle) VALUES
('Direction General','DG'),
('Ressources humaine','RH'),
('Informatique','IT'),
('Commercial','CM');

INSERT INTO poste(id_departement, libelle, sigle, salaire) VALUES
(1,'Directeur Generale','DG',10000000),
(2,'Responsable RH','RRH', 5000000),
(3,'Manager Informatique','MI',4000000),
(3,'Chef de projet','CP',3500000),
(3,'Dev Senior', 'DVS', 3000000),
(3,'Dev Junior', 'DJS', 1000000),
(4,'Manager Commerciale','MC',4000000),
(4,'Responsable de vente','RV', 3000000);

INSERT INTO employer (matricule,nom,prenom,telephone,compte,id_poste,salaire) VALUES
('EMP001','Maminirina','Michael','0345618902','CP001',1,10000000),
('EMP002','Rakotoarisoa','Koloina','0325315907','CP002',2,5500000),
('EMP003','Rajaonarivony','Paul','0375402134','CP003',3,4100000),
('EMP004','Rakotoarinelina','Fidele','0338712646','CP004',4,35000000),
('EMP005','Raminiarisoa','Noblette','03812368','CP005',5,3000000),
('EMP006','Rasoamiaramanana','Herinjatovo','0321543698','CP006',6,1000000),
('EMP007','Raveloson','Andry','03476581','CP007',7,4000000),
('EMP008','Rafidison','Tendry','0381945643','CP008',8,3000000);

INSERT INTO utilisateur(id_employer, email, mdp) VALUES
(1,'dg@gmail.com',crypt('dg', gen_salt('bf', 10))),
(2,'rh@gmail.com',crypt('rh', gen_salt('bf', 10))),
(3,'manager@gmail.com',crypt('manager', gen_salt('bf', 10)));

INSERT INTO utilisateur(email, mdp) VALUES
('candidat@gmail.com',crypt('candidat', gen_salt('bf', 10)));