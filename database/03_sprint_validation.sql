-- ============================================================================
--  Gestion-RH — migration du sprint Validation (RH / Finance / DG / Manager)
--
--  Converge une base issue du modele precedent vers la conception :
--    * ajout du role sur `utilisateur` ;
--    * renommage `detail_demande.motif` -> `detail_demande.description` ;
--    * `historique_demande_status` : `id_employer` nullable (l'expiration est
--      realisee par le systeme) + colonne `motif` (raison du refus) ;
--    * normalisation des statuts sur les 8 libelles du circuit ;
--    * creation des tables `roles`, `demande_a_revoir`, `grille_budget`,
--      `historique_budget_annuel` ;
--    * creation de la vue `v_demande_statut_courant`.
--
--  Le script est idempotent : il peut etre rejoue sans effet de bord.
--
--      psql -U postgres -d gestion_entreprise -f database/03_sprint_validation.sql
-- ============================================================================

-- ---------------------------------------------------------------------------
--  1. Roles
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id    SERIAL PRIMARY KEY,
    roles VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO roles (roles) VALUES ('RH'), ('Manager'), ('Finance'), ('DG')
ON CONFLICT (roles) DO NOTHING;

-- ---------------------------------------------------------------------------
--  2. `utilisateur.id_role` (ajout de role)
-- ---------------------------------------------------------------------------
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS id_role INT;

ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_id_role_fkey;
ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_id_role_fkey
    FOREIGN KEY (id_role) REFERENCES roles(id);

-- Le prefixe de l'adresse mail identifie le role (dg@, rh@, manager@, finance@).
UPDATE utilisateur u
SET id_role = r.id
FROM roles r
WHERE u.id_role IS NULL
  AND lower(split_part(u.email, '@', 1)) = lower(r.roles);

-- ---------------------------------------------------------------------------
--  3. `detail_demande.motif` -> `detail_demande.description` (renommage)
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'detail_demande' AND column_name = 'motif') THEN
        ALTER TABLE detail_demande RENAME COLUMN motif TO description;
    END IF;
END $$;

-- Colonnes de profilage des postes (presentes dans le schema de reference).
ALTER TABLE detail_demande ADD COLUMN IF NOT EXISTS id_diplome INT;
ALTER TABLE detail_demande ADD COLUMN IF NOT EXISTS annee_experience INT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                    WHERE conname = 'detail_demande_id_diplome_fkey') THEN
        ALTER TABLE detail_demande
            ADD CONSTRAINT detail_demande_id_diplome_fkey
            FOREIGN KEY (id_diplome) REFERENCES diplome(id);
    END IF;
END $$;

-- ---------------------------------------------------------------------------
--  4. `demande` : alignement sur le schema de reference.
--    Les colonnes commentaire / nbr_personnel_propose / traite avaient ete
--    ajoutees par un script intermediaire ; elles appartiennent desormais a
--    `demande_a_revoir` (une ligne par detail revise).
-- ---------------------------------------------------------------------------
ALTER TABLE demande DROP COLUMN IF EXISTS commentaire;
ALTER TABLE demande DROP COLUMN IF EXISTS nbr_personnel_propose;
ALTER TABLE demande DROP COLUMN IF EXISTS traite;

-- ---------------------------------------------------------------------------
--  5. `historique_demande_status` : qui a fait l'action, et pourquoi
-- ---------------------------------------------------------------------------
ALTER TABLE historique_demande_status ALTER COLUMN id_employer DROP NOT NULL;
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS motif TEXT;

CREATE INDEX IF NOT EXISTS historique_demande_status_demande_idx
    ON historique_demande_status (id_demande, date DESC, id DESC);

-- ---------------------------------------------------------------------------
--  6. Statuts : normalisation sur les 8 libelles du circuit de validation.
--    Les index uniques poses par un script intermediaire sont retires : ils
--    ne font pas partie du schema de reference et bloquent la normalisation.
-- ---------------------------------------------------------------------------
DROP INDEX IF EXISTS status_libelle_uq;
DROP INDEX IF EXISTS status_sigle_uq;

UPDATE status SET libelle = 'cree'            WHERE lower(libelle) IN ('creer', 'cree', 'créé');
UPDATE status SET libelle = 'valider rh'      WHERE lower(libelle) IN ('accepter');
UPDATE status SET libelle = 'valider finance' WHERE lower(libelle) IN ('valider', 'valide');
UPDATE status SET libelle = 'refuser'         WHERE lower(libelle) IN ('refuser', 'refuse', 'refusé');
UPDATE status SET libelle = 'a revoir'        WHERE lower(libelle) IN ('a revoir', 'a_revoir', 'à revoir');
UPDATE status SET libelle = 'abandone'        WHERE lower(libelle) IN ('abandone', 'abandonne', 'abandonné', 'annuler');
UPDATE status SET libelle = 'expire'          WHERE lower(libelle) IN ('expire', 'expiré');

INSERT INTO status (libelle, sigle) SELECT 'cree',            'CREE'            WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'cree');
INSERT INTO status (libelle, sigle) SELECT 'valider rh',      'VALIDER_RH'      WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'valider rh');
INSERT INTO status (libelle, sigle) SELECT 'valider finance', 'VALIDER_FINANCE' WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'valider finance');
INSERT INTO status (libelle, sigle) SELECT 'valider dg',      'VALIDER_DG'      WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'valider dg');
INSERT INTO status (libelle, sigle) SELECT 'a revoir',       'A_REVOIR'        WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'a revoir');
INSERT INTO status (libelle, sigle) SELECT 'refuser',        'REFUSER'         WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'refuser');
INSERT INTO status (libelle, sigle) SELECT 'abandone',       'ABANDONNE'       WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'abandone');
INSERT INTO status (libelle, sigle) SELECT 'expire',         'EXPIRE'          WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = 'expire');

-- Suppression des doublons : l'historique est reapointé sur le statut retenu.
WITH doublons AS (
    SELECT id, MIN(id) OVER (PARTITION BY lower(libelle)) AS id_retenu
    FROM status
)
UPDATE historique_demande_status h
SET id_status = d.id_retenu
FROM doublons d
WHERE h.id_status = d.id AND d.id <> d.id_retenu;

WITH doublons AS (
    SELECT id, MIN(id) OVER (PARTITION BY lower(libelle)) AS id_retenu
    FROM status
)
DELETE FROM status s
USING doublons d
WHERE s.id = d.id AND d.id <> d.id_retenu;

UPDATE status SET sigle = 'CREE'            WHERE lower(libelle) = 'cree';
UPDATE status SET sigle = 'VALIDER_RH'      WHERE lower(libelle) = 'valider rh';
UPDATE status SET sigle = 'VALIDER_FINANCE' WHERE lower(libelle) = 'valider finance';
UPDATE status SET sigle = 'VALIDER_DG'      WHERE lower(libelle) = 'valider dg';
UPDATE status SET sigle = 'A_REVOIR'        WHERE lower(libelle) = 'a revoir';
UPDATE status SET sigle = 'REFUSER'         WHERE lower(libelle) = 'refuser';
UPDATE status SET sigle = 'ABANDONNE'       WHERE lower(libelle) = 'abandone';
UPDATE status SET sigle = 'EXPIRE'          WHERE lower(libelle) = 'expire';

-- ---------------------------------------------------------------------------
--  7. `demande_a_revoir` : lignes a corriger (une ligne par detail revise)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS demande_a_revoir (
    id                    SERIAL PRIMARY KEY,
    id_demandeur          INT NOT NULL REFERENCES employer(id),
    id_demande_details    INT NOT NULL REFERENCES detail_demande(id) ON DELETE CASCADE,
    date                  DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire           TEXT,
    nbr_personnel_propose INT
);

ALTER TABLE demande_a_revoir DROP COLUMN IF EXISTS traite;

CREATE INDEX IF NOT EXISTS demande_a_revoir_detail_idx
    ON demande_a_revoir (id_demande_details);

-- ---------------------------------------------------------------------------
--  8. Budgets
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS grille_budget (
    id             SERIAL PRIMARY KEY,
    id_poste       INT NOT NULL REFERENCES poste(id),
    budget_min     DECIMAL(15,2) NOT NULL,
    budget_max     DECIMAL(15,2) NOT NULL,
    date_effective DATE NOT NULL,
    CHECK (budget_min <= budget_max)
);

CREATE INDEX IF NOT EXISTS grille_budget_poste_idx
    ON grille_budget (id_poste, date_effective DESC);

CREATE TABLE IF NOT EXISTS historique_budget_annuel (
    id             SERIAL PRIMARY KEY,
    valeur         DECIMAL(15,2) NOT NULL,
    date_effective DATE NOT NULL
);

-- ---------------------------------------------------------------------------
--  9. Nettoyage des tables intermediaires hors schema de reference
-- ---------------------------------------------------------------------------
DROP VIEW IF EXISTS v_demande_statut_courant;
DROP TABLE IF EXISTS demande_statut;

-- ---------------------------------------------------------------------------
--  10. Vues
-- ---------------------------------------------------------------------------
CREATE OR REPLACE VIEW v_historique_demande_status AS
SELECT
    h.id              AS id,
    h.id_demande      AS id_demande,
    h.id_status       AS id_status,
    h.id_employer     AS id_employer_evaluateur,
    e.nom             AS nom_evaluateur,
    e.prenom          AS prenom_evaluateur,
    d.matricule       AS matricule,
    d.id_employer     AS id_employer,
    s.libelle         AS libelle,
    h.date            AS date
FROM historique_demande_status h
JOIN demande  d ON d.id = h.id_demande
JOIN status   s ON s.id = h.id_status
JOIN employer e ON e.id = h.id_employer;

CREATE OR REPLACE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (h.id_demande)
       h.id_demande,
       h.id_status,
       s.libelle,
       s.sigle,
       h.id_employer,
       h.date,
       h.motif
FROM historique_demande_status h
JOIN status s ON s.id = h.id_status
ORDER BY h.id_demande, h.date DESC, h.id DESC;
