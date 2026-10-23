-- =============================================================
--  BLOC 3 : Proposition d'embauche + Contrat PDF
-- =============================================================

CREATE TABLE proposition_embauche_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO proposition_embauche_statut (libelle) VALUES
('EN_ATTENTE_CANDIDAT'),
('EN_NEGOCIATION'),
('EN_VALIDATION_RH'),
('ACCEPTEE'),
('REFUSEE'),
('EXPIREE');

CREATE TABLE reponse_type (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(20) NOT NULL UNIQUE
);
INSERT INTO reponse_type (libelle) VALUES ('OUI'), ('NON'), ('AUTRE');

CREATE TABLE contrat_type (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(10) NOT NULL UNIQUE
);
INSERT INTO contrat_type (libelle) VALUES ('CE'), ('CDD'), ('CDI');

CREATE TABLE parametre (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    valeur TEXT NOT NULL
);
INSERT INTO parametre (code, valeur) VALUES
('NOM_ENTREPRISE', 'PIVOT SARL'),
('ADRESSE_ENTREPRISE', 'Lot II M 45 Bis, Antananarivo 101, Madagascar'),
('NUM_COMPTE_COTISANT', '123456789012345'),
('REPRESENTANT_ENTREPRISE', 'M. Michael Maminirina, Directeur Général'),
('DEVISE', 'MGA'),
('CONVENTION_COLLECTIVE', 'SYNTEC Madagascar'),
('CAISSE_RETRAITE', 'CRCM'),
('ORGANISME_PREVOYANCE', 'Arovana Prévoyance');

CREATE TABLE proposition_embauche (
    id SERIAL PRIMARY KEY,
    id_candidat INT NOT NULL,
    id_contrat_type INT NOT NULL DEFAULT 1,
    salaire NUMERIC(15,2) NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    lieu_travail VARCHAR(255) NOT NULL,
    horaires VARCHAR(255) NOT NULL,
    duree_essai_mois INT NOT NULL DEFAULT 6,
    date_expiration TIMESTAMP NOT NULL,
    id_proposition_embauche_statut INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_candidat) REFERENCES candidat(id),
    FOREIGN KEY (id_contrat_type) REFERENCES contrat_type(id),
    FOREIGN KEY (id_proposition_embauche_statut) REFERENCES proposition_embauche_statut(id)
);

CREATE TABLE proposition_embauche_question (
    id SERIAL PRIMARY KEY,
    id_proposition_embauche INT NOT NULL,
    ordre INT NOT NULL,
    question TEXT NOT NULL,
    id_reponse_type INT,
    remarque TEXT,
    FOREIGN KEY (id_proposition_embauche) REFERENCES proposition_embauche(id),
    FOREIGN KEY (id_reponse_type) REFERENCES reponse_type(id)
);

CREATE TABLE proposition_embauche_negociation (
    id SERIAL PRIMARY KEY,
    id_proposition_embauche INT NOT NULL,
    remarques_candidat TEXT NOT NULL,
    reponse_rh TEXT,
    accepte BOOLEAN,
    date_negociation TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_proposition_embauche) REFERENCES proposition_embauche(id)
);

CREATE TABLE historique_proposition_embauche_statut (
    id SERIAL PRIMARY KEY,
    id_proposition_embauche INT NOT NULL,
    id_proposition_embauche_statut INT NOT NULL,
    id_utilisateur INT,
    motif TEXT,
    date_changement TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_proposition_embauche) REFERENCES proposition_embauche(id),
    FOREIGN KEY (id_proposition_embauche_statut) REFERENCES proposition_embauche_statut(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id)
);