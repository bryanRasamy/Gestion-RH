-- ============================================================================
--  Gestion-RH — schema de reference
--
--  Regle de ce sprint : les tables existantes ne sont pas modifiees, a
--  l'exception des changements explicitement demandes par la conception :
--    1. ajout de la colonne de role sur `utilisateur` (FK vers `roles`) ;
--    2. renommage de `detail_demande.motif` en `detail_demande.description` ;
--    3. `historique_demande_status.id_employer` devient nullable (l'expiration
--       est realisee par le systeme) et gagne une colonne `motif` (raison du
--       refus / commentaire de la revision).
--  Tout le reste est purement additif (nouvelles tables, nouvelles vues).
--
--  Execution : psql -U postgres -f database/table.sql
--  Le script est rejouable (CREATE ... IF NOT EXISTS).
-- ============================================================================

SELECT 'CREATE DATABASE gestion_entreprise'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'gestion_entreprise')\gexec

\connect gestion_entreprise

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
--  1. Referentiel : roles
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id   SERIAL PRIMARY KEY,
    roles VARCHAR(30) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------------
--  2. Referentiel : departement / poste / employer / diplome
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departement (
    id     SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle  VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS poste (
    id            SERIAL PRIMARY KEY,
    id_departement INT NOT NULL,
    libelle       VARCHAR(100) NOT NULL,
    sigle         VARCHAR(20) NOT NULL,
    salaire       DECIMAL(10,2),
    FOREIGN KEY (id_departement) REFERENCES departement(id)
);

CREATE TABLE IF NOT EXISTS employer (
    id        SERIAL PRIMARY KEY,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    nom       VARCHAR(100) NOT NULL,
    prenom    VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    compte    VARCHAR(100),
    id_poste  INT NOT NULL,
    salaire   DECIMAL(10,2),
    FOREIGN KEY (id_poste) REFERENCES poste(id)
);

CREATE TABLE IF NOT EXISTS diplome (
    id          SERIAL PRIMARY KEY,
    annee_etude INT NOT NULL,
    nom_diplome VARCHAR(100) NOT NULL
);

-- ---------------------------------------------------------------------------
--  3. Utilisateurs : ajout de la colonne de role (seule modification de la
--     table `utilisateur` autorisee par la conception).
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS utilisateur (
    id         SERIAL PRIMARY KEY,
    id_employer INT,
    email      VARCHAR(150) NOT NULL UNIQUE,
    mdp        VARCHAR(255) NOT NULL,
    id_role    INT,
    FOREIGN KEY (id_employer) REFERENCES employer(id),
    FOREIGN KEY (id_role) REFERENCES roles(id)
);

-- ---------------------------------------------------------------------------
--  4. Statuts et demandes
-- ---------------------------------------------------------------------------

-- Liste des statuts du circuit de validation : cree, valider rh,
-- valider finance, valider dg, a revoir, refuser, abandone, expire.
CREATE TABLE IF NOT EXISTS status (
    id      SERIAL PRIMARY KEY,
    libelle VARCHAR(100) NOT NULL,
    sigle   VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS demande (
    id          SERIAL PRIMARY KEY,
    matricule   VARCHAR(50) NOT NULL UNIQUE,
    id_employer INT NOT NULL,
    date_limite DATE,
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

-- `motif` a ete renomme `description` (conception du sprint).
CREATE TABLE IF NOT EXISTS detail_demande (
    id               SERIAL PRIMARY KEY,
    id_demande       INT NOT NULL,
    id_poste         INT NOT NULL,
    id_departement   INT NOT NULL,
    nbr_personnel    INT NOT NULL,
    description      TEXT,
    id_diplome       INT NOT NULL,
    annee_experience INT NOT NULL,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_poste) REFERENCES poste(id),
    FOREIGN KEY (id_departement) REFERENCES departement(id),
    FOREIGN KEY (id_diplome) REFERENCES diplome(id)
);

-- Historique du circuit : une ligne par action realisee sur la demande.
-- `id_employer` = la personne qui a realise l'action (NULL si le systeme).
-- `motif` = raison du refus ou commentaire de la revision.
CREATE TABLE IF NOT EXISTS historique_demande_status (
    id          SERIAL PRIMARY KEY,
    id_demande  INT NOT NULL,
    id_status   INT NOT NULL,
    id_employer INT,
    date        DATE NOT NULL DEFAULT CURRENT_DATE,
    motif       TEXT,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_status) REFERENCES status(id),
    FOREIGN KEY (id_employer) REFERENCES employer(id)
);

CREATE INDEX IF NOT EXISTS historique_demande_status_demande_idx
    ON historique_demande_status (id_demande, date DESC, id DESC);

-- ---------------------------------------------------------------------------
--  5. Nouvelles tables du sprint (conception)
-- ---------------------------------------------------------------------------

-- 5.1 Lignes a corriger : une ligne par detail de demande revise.
--     `id_demandeur` = l'employe (RH / Finance / DG) qui demande la revision.
--     Pas de colonne `traite` : une revision en attente se deduit du statut
--     courant de la demande (`a revoir`).
CREATE TABLE IF NOT EXISTS demande_a_revoir (
    id                    SERIAL PRIMARY KEY,
    id_demandeur          INT NOT NULL REFERENCES employer(id),
    id_demande_details    INT NOT NULL REFERENCES detail_demande(id) ON DELETE CASCADE,
    date                  DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire           TEXT,
    nbr_personnel_propose INT
);

CREATE INDEX IF NOT EXISTS demande_a_revoir_detail_idx
    ON demande_a_revoir (id_demande_details);

-- 5.2 Fourchette budgetaire par poste (historise par date d'efficacite).
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

-- 5.3 Budget global annuel (la ligne la plus recente <= aujourd'hui fait foi).
CREATE TABLE IF NOT EXISTS historique_budget_annuel (
    id             SERIAL PRIMARY KEY,
    valeur         DECIMAL(15,2) NOT NULL,
    date_effective DATE NOT NULL
);

-- ---------------------------------------------------------------------------
--  6. Vues
-- ---------------------------------------------------------------------------

-- Vue historique heritee (conservee).
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

-- Statut courant d'une demande : derniere ligne de l'historique.
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
