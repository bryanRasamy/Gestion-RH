-- Sprint workflow RH / Finance / DG / Manager.
-- A executer apres le schema initial, puis les donnees de reference.

CREATE TABLE IF NOT EXISTS roles (
    id SERIAL PRIMARY KEY,
    roles VARCHAR(30) NOT NULL UNIQUE
);
INSERT INTO roles (roles) VALUES ('RH'), ('Manager'), ('Finance'), ('DG')
ON CONFLICT (roles) DO NOTHING;

ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS id_role INT;

-- ⚠️ L'UPDATE ci-dessous ne fonctionne que si la colonne "role" existe encore.
--    Sur une base déjà migrée, il échouera (colonne absente).
UPDATE utilisateur u SET id_role = r.id
FROM roles r
WHERE lower(u.role) = lower(r.roles) AND u.id_role IS NULL;

ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_fkey;
ALTER TABLE utilisateur DROP COLUMN IF EXISTS role;

ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_id_role_fkey;
ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_id_role_fkey
    FOREIGN KEY (id_role) REFERENCES roles(id);

UPDATE utilisateur u SET id_role = r.id FROM roles r
WHERE u.email = 'dg@gmail.com' AND r.roles = 'DG' AND u.id_role IS NULL;
UPDATE utilisateur u SET id_role = r.id FROM roles r
WHERE u.email = 'rh@gmail.com' AND r.roles = 'RH' AND u.id_role IS NULL;
UPDATE utilisateur u SET id_role = r.id FROM roles r
WHERE u.email = 'manager@gmail.com' AND r.roles = 'Manager' AND u.id_role IS NULL;

CREATE TABLE IF NOT EXISTS status (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(30) NOT NULL,
    sigle VARCHAR(30) NOT NULL
);
ALTER TABLE status ADD COLUMN IF NOT EXISTS sigle VARCHAR(30);
UPDATE status SET libelle = 'a revoir' WHERE lower(libelle) IN ('a_revoir', 'à revoir');
UPDATE status SET libelle = 'refuse' WHERE lower(libelle) IN ('refusé', 'refuse');
UPDATE status SET libelle = 'abandone' WHERE lower(libelle) IN ('abandonne', 'abandonné', 'abandone');
UPDATE status SET libelle = 'expire' WHERE lower(libelle) IN ('expiré', 'expire');
UPDATE status SET libelle = 'valider' WHERE lower(libelle) IN ('valide', 'validé', 'valider', 'validé rh', 'validé finance', 'validé dg');
DELETE FROM status WHERE lower(libelle) NOT IN ('a revoir', 'refuse', 'abandone', 'expire', 'valider');
UPDATE status SET sigle = 'A_REVOIR' WHERE libelle = 'a revoir';
UPDATE status SET sigle = 'REFUSE' WHERE libelle = 'refuse';
UPDATE status SET sigle = 'ABANDONE' WHERE libelle = 'abandone';
UPDATE status SET sigle = 'EXPIRE' WHERE libelle = 'expire';
UPDATE status SET sigle = 'VALIDER' WHERE libelle = 'valider';
INSERT INTO status (libelle, sigle) SELECT 'a revoir', 'A_REVOIR'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE libelle = 'a revoir');
INSERT INTO status (libelle, sigle) SELECT 'refuse', 'REFUSE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE libelle = 'refuse');
INSERT INTO status (libelle, sigle) SELECT 'abandone', 'ABANDONE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE libelle = 'abandone');
INSERT INTO status (libelle, sigle) SELECT 'expire', 'EXPIRE'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE libelle = 'expire');
INSERT INTO status (libelle, sigle) SELECT 'valider', 'VALIDER'
WHERE NOT EXISTS (SELECT 1 FROM status WHERE libelle = 'valider');
DELETE FROM status ancien USING status doublon
WHERE ancien.libelle = doublon.libelle AND ancien.id > doublon.id;
ALTER TABLE status ALTER COLUMN sigle SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS status_libelle_uq ON status(libelle);
CREATE UNIQUE INDEX IF NOT EXISTS status_sigle_uq ON status(sigle);

ALTER TABLE demande ADD COLUMN IF NOT EXISTS commentaire TEXT;
ALTER TABLE demande ADD COLUMN IF NOT EXISTS nbr_personnel_propose INT;
ALTER TABLE demande ADD COLUMN IF NOT EXISTS traite BOOLEAN NOT NULL DEFAULT FALSE;

-- ⚠️ RENAME COLUMN n'a pas de IF EXISTS : échouera si "motif" n'existe pas
--    (base déjà migrée). Sur une base vierge (schema initial), c'est OK.
ALTER TABLE detail_demande RENAME COLUMN motif TO description;

CREATE TABLE IF NOT EXISTS demande_statut (
    id SERIAL PRIMARY KEY,
    id_demande INT NOT NULL REFERENCES demande(id),
    libelle VARCHAR(30) NOT NULL,
    id_employe INT REFERENCES employer(id),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    motif TEXT
);
ALTER TABLE demande_statut ADD COLUMN IF NOT EXISTS motif TEXT;
ALTER TABLE demande_statut ALTER COLUMN id_employe DROP NOT NULL;

CREATE TABLE IF NOT EXISTS demande_a_revoir (
    id SERIAL PRIMARY KEY,
    id_demandeur INT NOT NULL REFERENCES employer(id),
    id_demande_details INT NOT NULL REFERENCES detail_demande(id),
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire TEXT,
    nbr_personnel_propose INT,
    traite BOOLEAN NOT NULL DEFAULT FALSE
);

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

UPDATE demande_statut SET libelle = 'a revoir' WHERE lower(libelle) IN ('a_revoir', 'à revoir');
UPDATE demande_statut SET libelle = 'refuse' WHERE lower(libelle) IN ('refusé', 'refuse');
UPDATE demande_statut SET libelle = 'abandone' WHERE lower(libelle) IN ('abandonne', 'abandonné', 'abandone');
UPDATE demande_statut SET libelle = 'expire' WHERE lower(libelle) IN ('expiré', 'expire');
UPDATE demande_statut SET libelle = 'valider' WHERE lower(libelle) IN ('valide', 'validé', 'valider', 'valide_rh', 'valide_finance', 'valide_dg');
DELETE FROM demande_statut WHERE lower(libelle) NOT IN ('a revoir', 'refuse', 'abandone', 'expire', 'valider');

CREATE OR REPLACE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (id_demande) id_demande, libelle, id_employe, date, motif
FROM demande_statut
ORDER BY id_demande, date DESC, id DESC;