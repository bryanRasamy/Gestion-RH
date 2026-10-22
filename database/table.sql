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
    libelle VARCHAR(100) NOT NULL
);

INSERT INTO status(libelle) VALUES
('créé'), ('validé RH'), ('validé Finance'), ('validé DG'),
('à revoir'), ('refusé'), ('abandonné'), ('expiré');

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
    description TEXT,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE historique_demande_status(
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL,
    id_status INT NOT NULL,
    id_employer INT,
    motif TEXT,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_status) REFERENCES status(id),
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE TABLE demande_a_revoir(
    id SERIAL PRIMARY KEY,
    id_mpangataka INT NOT NULL,
    id_detail_demande INT NOT NULL,
    etape_demandeuse VARCHAR(20) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire TEXT NOT NULL,
    nbr_personnel_propose INT NOT NULL DEFAULT 0,
    annuler_poste BOOLEAN NOT NULL DEFAULT FALSE,
    traite BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_mpangataka) REFERENCES employer(id),
    FOREIGN KEY (id_detail_demande) REFERENCES detail_demande(id)
);

CREATE TABLE offre(
    id SERIAL PRIMARY KEY,
    id_detail_demande INT NOT NULL UNIQUE,
    date_limite DATE,
    FOREIGN KEY (id_detail_demande) REFERENCES detail_demande(id)
);

CREATE TABLE grille_budget(
    id SERIAL PRIMARY KEY,
    id_poste INT NOT NULL,
    budget_min DOUBLE PRECISION NOT NULL,
    budget_max DOUBLE PRECISION NOT NULL,
    date_effective DATE NOT NULL,
    CHECK (budget_min <= budget_max),
    FOREIGN KEY (id_poste) REFERENCES poste(id)
);

CREATE TABLE parametre(
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    valeur DOUBLE PRECISION NOT NULL,
    date_effective DATE NOT NULL
);

INSERT INTO parametre(code, valeur, date_effective) VALUES ('BUDGET', 0, CURRENT_DATE);

CREATE VIEW v_historique_demande_status AS
SELECT
    h.id,
    h.id_demande,
    h.id_status,
    d.matricule,
    d.id_employer,
    s.libelle,
    h.date
FROM historique_demande_status h
JOIN demande d ON d.id = h.id_demande
JOIN status s ON s.id = h.id_status;

CREATE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (h.id_demande)
    h.id_demande,
    h.id_status,
    s.libelle AS statut,
    h.id,
    h.date
FROM historique_demande_status h
JOIN status s ON s.id = h.id_status
ORDER BY h.id_demande, h.id DESC;
