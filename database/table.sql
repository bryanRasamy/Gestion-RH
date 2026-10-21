CREATE DATABASE gestion_entreprise;

\c gestion_entreprise

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE departement(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle VARCHAR(20) NOT NULL
);

CREATE TABLE poste(
    id SERIAL PRIMARY KEY,
    id_departement INT NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    sigle VARCHAR(20) NOT NULL,
    salaire DECIMAL(10,2),
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE employer(
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    compte VARCHAR(100),
    id_poste INT NOT NULL,
    salaire DECIMAL(10,2),
    FOREIGN KEY (id_poste) REFERENCES poste(id)
);

CREATE TABLE utilisateur(
    id SERIAL PRIMARY KEY,
    id_employer INT,
    email VARCHAR(150) NOT NULL UNIQUE,
    mdp VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE TABLE status(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle VARCHAR(20) NOT NULL
);

CREATE TABLE demARCHAR(50)ande(
    id SERIAL PRIMARY KEY,
    matricule V NOT NULL UNIQUE,
    id_employer INT NOT NULL,
    date_limite DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE TABLE detail_demande(
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_poste INT NOT NULL,
    id_departement INT NOT NULL,
    nbr_personnel INT NOT NULL,
    motif TEXT,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE historique_demande_status(
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_status INT NOT NULL,
    id_employer INT NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_status) REFERENCES status(id),
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE TABLE candidat(
    id SERIAL PRIMARY KEY,
    id_user INT NOT NULL,
    id_detail_demande INT NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    cv VARCHAR(100),
    lm VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'En attente',
    FOREIGN KEY (id_user) REFERENCES utilisateur(id)
    FOREIGN KEY (id_detail_demande) REFERENCES detail_demande(id)
);

CREATE VIEW v_historique_demande_status AS
SELECT
    h.id as id,
    h.id_demande as id_demande,
    h.id_status as id_status,
    h.id_employer as id_employer_evaluateur,
    e.nom as nom_evaluateur,
    e.prenom as prenom_evaluateur,
    d.matricule as matricule,
    d.id_employer as id_employer,
    s.libelle as libelle,
    h.date as date
FROM historique_demande_status h
JOIN demande d ON d.id = h.id_demande
JOIN status s ON s.id = h.id_status
JOIN employer e ON e.id=h.id_employer;