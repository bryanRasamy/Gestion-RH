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
    salaire NUMERIC(15,2),
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
    salaire NUMERIC(15,2),
    FOREIGN KEY (id_poste) REFERENCES poste(id)
);

CREATE TABLE role(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE utilisateur(
    id SERIAL PRIMARY KEY,
    id_employer INT,
    id_role INT,
    email VARCHAR(150) NOT NULL UNIQUE,
    mdp VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id),
    FOREIGN KEY (id_role) REFERENCES role(id)
);

CREATE TABLE status(
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle VARCHAR(20) NOT NULL
);

CREATE TABLE demande(
    id SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    id_employer INT NOT NULL,
    date_limite DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE TABLE diplome(
    id SERIAL PRIMARY KEY,
    annee_etude INT NOT NULL,
    nom_diplome VARCHAR(100) NOT NULL
);

CREATE TABLE detail_demande(
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_poste INT NOT NULL,
    id_departement INT NOT NULL,
    nbr_personnel INT NOT NULL,
    motif TEXT,
    id_diplome INT NOT NULL,
    annee_experience INT NOT NULL,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE historique_demande_status(
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_status INT NOT NULL,
    id_employer INT NOT NULL,
    motif TEXT,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_status) REFERENCES status(id),
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

-- Snapshot d'une detail_demande jugée "à revoir" par le correcteur
CREATE TABLE demande_a_revoir(
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL,
    id_demande_details INT NOT NULL,
    nb_souhaite INT NOT NULL,
    id_diplome INT NOT NULL,
    nb_experience INT NOT NULL,
    id_date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_demandeur) REFERENCES employer(id),
    FOREIGN KEY (id_demande_details) REFERENCES detail_demande(id),
    FOREIGN KEY (id_diplome) REFERENCES diplome(id)
);

-- Budget max/min associé à un poste, avec date d'effectivité
CREATE TABLE grille_budget(
    id SERIAL PRIMARY KEY,
    id_poste INT NOT NULL,
    budget_min NUMERIC(15,2) NOT NULL,
    budget_max NUMERIC(15,2) NOT NULL,
    date_effective DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_poste) REFERENCES poste(id)
);

-- Budget annuel global de l'entreprise
CREATE TABLE historique_budget_annuel(
    id SERIAL PRIMARY KEY,
    valeur NUMERIC(15,2) NOT NULL,
    date_effective DATE NOT NULL DEFAULT CURRENT_DATE
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
    s.sigle as sigle,
    h.motif as motif,
    h.date as date
FROM historique_demande_status h
JOIN demande d ON d.id = h.id_demande
JOIN status s ON s.id = h.id_status
JOIN employer e ON e.id=h.id_employer;