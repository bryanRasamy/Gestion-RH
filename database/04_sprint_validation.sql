-- Sprint Validation RH / Finance / DG / Manager
-- Exécuter après table.sql + donnees.sql.

-- 1) Rôles et liaison utilisateur
CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    roles VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (roles) VALUES
('RH'), ('Manager'), ('Finance'), ('DG')
ON CONFLICT (roles) DO NOTHING;

ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS id_role INT;
ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_id_role_fkey;
ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_id_role_fkey
    FOREIGN KEY (id_role) REFERENCES roles(id);

-- Mapping des comptes de test
UPDATE utilisateur u
SET id_role = r.id
FROM roles r
WHERE u.email = 'dg@gmail.com' AND r.roles = 'DG' AND u.id_role IS NULL;

UPDATE utilisateur u
SET id_role = r.id
FROM roles r
WHERE u.email = 'rh@gmail.com' AND r.roles = 'RH' AND u.id_role IS NULL;

UPDATE utilisateur u
SET id_role = r.id
FROM roles r
WHERE u.email = 'manager@gmail.com' AND r.roles = 'Manager' AND u.id_role IS NULL;

-- 2) Renommage motif -> description sur detail_demande
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'detail_demande' AND column_name = 'motif'
    ) THEN
        ALTER TABLE detail_demande RENAME COLUMN motif TO description;
    END IF;
END $$;

-- 3) Colonnes de correction / suivi de traitement
ALTER TABLE demande ADD COLUMN IF NOT EXISTS commentaire TEXT;
ALTER TABLE demande ADD COLUMN IF NOT EXISTS nbr_personnel_propose INT;
ALTER TABLE demande ADD COLUMN IF NOT EXISTS traite BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS demande_a_revoir (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL REFERENCES employer(id),
    id_demande_details INT NOT NULL REFERENCES detail_demande(id),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire TEXT,
    nbr_personnel_propose INT,
    traite BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4) Table de statut demandée par la conception
CREATE TABLE IF NOT EXISTS demande_statut (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL REFERENCES demande(id),
    libelle VARCHAR(30) NOT NULL,
    id_employe INT REFERENCES employer(id),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    motif TEXT
);

-- 5) Migration optionnelle depuis historique_demande_status (ancien modèle)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'historique_demande_status')
       AND EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'historique_demande_status' AND column_name = 'id_status')
       AND NOT EXISTS (SELECT 1 FROM demande_statut)
    THEN
        INSERT INTO demande_statut (id_demande, libelle, id_employe, date, motif)
        SELECT h.id_demande,
               lower(s.libelle),
               h.id_employer,
               h.date,
               NULL
        FROM historique_demande_status h
        JOIN status s ON s.id = h.id_status;
    END IF;
END $$;

-- 6) Normalisation des libellés de statut
CREATE UNIQUE INDEX IF NOT EXISTS status_libelle_uq ON status (lower(libelle));

INSERT INTO status (libelle, sigle)
SELECT 'cree', 'CREE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'cree');

INSERT INTO status (libelle, sigle)
SELECT 'valider', 'VALIDER'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'valider');

INSERT INTO status (libelle, sigle)
SELECT 'a revoir', 'A_REVOIR'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'a revoir');

INSERT INTO status (libelle, sigle)
SELECT 'refuser', 'REFUSER'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'refuser');

INSERT INTO status (libelle, sigle)
SELECT 'abandone', 'ABANDONE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'abandone');

INSERT INTO status (libelle, sigle)
SELECT 'expire', 'EXPIRE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'expire');

-- Harmonise des valeurs historiques courantes
UPDATE demande_statut SET libelle = 'a revoir' WHERE lower(libelle) IN ('a_revoir', 'à revoir');
UPDATE demande_statut SET libelle = 'refuser' WHERE lower(libelle) IN ('refuse', 'refusé');
UPDATE demande_statut SET libelle = 'abandone' WHERE lower(libelle) IN ('abandonne', 'abandonné');
UPDATE demande_statut SET libelle = 'cree' WHERE lower(libelle) IN ('creer', 'créé', 'cree');
UPDATE demande_statut SET libelle = 'valider' WHERE lower(libelle) IN ('validé', 'valide');

-- 7) Budgets
CREATE TABLE IF NOT EXISTS grille_budget (
    id SERIAL PRIMARY KEY,
    id_poste INT NOT NULL REFERENCES poste(id),
    budget_min DECIMAL(15,2) NOT NULL,
    budget_max DECIMAL(15,2) NOT NULL,
    date_effective DATE NOT NULL,
    CHECK (budget_min <= budget_max)
);

CREATE TABLE IF NOT EXISTS historique_budget_annuel (
    id SERIAL PRIMARY KEY,
    valeur DECIMAL(15,2) NOT NULL,
    date_effective DATE NOT NULL
);

-- 8) Vue statut courant
CREATE OR REPLACE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (id_demande)
       id,
       id_demande,
       libelle,
       id_employe,
       date,
       motif
FROM demande_statut
ORDER BY id_demande, date DESC, id DESC;
