CREATE DATABASE gestion_entreprise;

\c gestion_entreprise

CREATE TABLE departement(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE poste(
    id SERIAL PRIMARY KEY,
    id_departement INT NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    salaire INT,
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE employer(
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    telephone VARCHAR(20),
    compte VARCHAR(100),
    id_poste INT NOT NULL,
    salaire INT,
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
    libelle VARCHAR(100) NOT NULL
);

CREATE TABLE demande(
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
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
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_status) REFERENCES status(id)
);

CREATE VIEW v_historique_demande_status AS
SELECT
    h.id,
    h.id_demande,
    h.id_status,
    d.matricule,
    d.id_employer,
    s.libelle,
    h.date_status
FROM historique_demande_status h
JOIN demande d ON d.id = h.id_demande
JOIN status s ON s.id = h.id_status;