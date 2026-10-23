-- =============================================================
--  BLOC 4 : Contrat, signature, validation, onboarding
-- =============================================================

-- Statuts du contrat
CREATE TABLE contrat_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO contrat_statut (libelle) VALUES
('CREE'),
('SIGNE_PAR_CANDIDAT'),
('VALIDE_PAR_DG'),
('ANNULE');

-- Historique du contrat (append-only)
CREATE TABLE historique_contrat_statut (
    id SERIAL PRIMARY KEY,
    id_contrat INT NOT NULL,
    id_contrat_statut INT NOT NULL,
    id_utilisateur INT,
    motif TEXT,
    date_changement TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_contrat) REFERENCES contrat(id),
    FOREIGN KEY (id_contrat_statut) REFERENCES contrat_statut(id),
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id)
);

-- Ajouter la colonne statut + suivi au contrat existant
ALTER TABLE contrat ADD COLUMN IF NOT EXISTS id_contrat_statut INT;
UPDATE contrat SET id_contrat_statut = (SELECT id FROM contrat_statut WHERE libelle = 'CREE')
WHERE id_contrat_statut IS NULL;
ALTER TABLE contrat ALTER COLUMN id_contrat_statut SET NOT NULL;
ALTER TABLE contrat ADD CONSTRAINT fk_contrat_statut
    FOREIGN KEY (id_contrat_statut) REFERENCES contrat_statut(id);

-- Statuts de la période d'emploi
CREATE TABLE periode_emploi_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO periode_emploi_statut (libelle) VALUES
('PRE_EMBAUCHE'),
('ACTIVE'),
('TERMINEE'),
('ANNULEE');

-- Période d'emploi (un cycle entrée → sortie)
CREATE TABLE periode_emploi (
    id SERIAL PRIMARY KEY,
    id_employer INT NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP,
    id_periode_emploi_statut INT NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id),
    FOREIGN KEY (id_periode_emploi_statut) REFERENCES periode_emploi_statut(id)
);

-- Statuts de l'affectation
CREATE TABLE affectation_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO affectation_statut (libelle) VALUES
('EN_ATTENTE_PREREQUIS'),
('ACTIVE'),
('TERMINEE');

-- Affectation : quel employé sur quel poste, pour quel contrat
CREATE TABLE affectation (
    id SERIAL PRIMARY KEY,
    id_periode_emploi INT NOT NULL,
    id_poste INT NOT NULL,
    id_contrat INT,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP,
    id_affectation_statut INT NOT NULL,
    FOREIGN KEY (id_periode_emploi) REFERENCES periode_emploi(id),
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_contrat) REFERENCES contrat(id),
    FOREIGN KEY (id_affectation_statut) REFERENCES affectation_statut(id)
);

-- Badge
CREATE TABLE badge_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO badge_statut (libelle) VALUES
('ACTIF'),
('SUSPENDU'),
('PERDU'),
('RESTITUE'),
('EXPIRE');

CREATE TABLE badge (
    id SERIAL PRIMARY KEY,
    id_employer INT NOT NULL,
    numero VARCHAR(50) NOT NULL UNIQUE,
    date_emission TIMESTAMP NOT NULL,
    date_expiration TIMESTAMP,
    id_badge_statut INT NOT NULL,
    FOREIGN KEY (id_employer) REFERENCES employer(id),
    FOREIGN KEY (id_badge_statut) REFERENCES badge_statut(id)
);

-- Checklist onboarding
CREATE TABLE onboarding_tache_type (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL UNIQUE,
    ordre INT NOT NULL DEFAULT 0,
    obligatoire BOOLEAN NOT NULL DEFAULT TRUE,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);
INSERT INTO onboarding_tache_type (libelle, ordre, obligatoire) VALUES
('Contrat signé et validé', 1, TRUE),
('Dossier administratif complet', 2, TRUE),
('Badge remis', 3, TRUE),
('Compte bancaire', 4, FALSE),
('Matériel attribué', 5, TRUE),
('Accès et comptes informatiques', 6, TRUE),
('Formation initiale', 7, FALSE),
('Affiliation sociale', 8, TRUE),
('Accueil / visite des locaux', 9, FALSE),
('Inscription à la paie', 10, TRUE);

CREATE TABLE onboarding_tache_statut (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL UNIQUE
);
INSERT INTO onboarding_tache_statut (libelle) VALUES
('A_FAIRE'),
('EN_COURS'),
('TERMINEE'),
('NON_APPLICABLE');

CREATE TABLE onboarding_tache (
    id SERIAL PRIMARY KEY,
    id_periode_emploi INT NOT NULL,
    id_onboarding_tache_type INT NOT NULL,
    id_onboarding_tache_statut INT NOT NULL,
    date_realisation TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_periode_emploi) REFERENCES periode_emploi(id),
    FOREIGN KEY (id_onboarding_tache_type) REFERENCES onboarding_tache_type(id),
    FOREIGN KEY (id_onboarding_tache_statut) REFERENCES onboarding_tache_statut(id)
);

