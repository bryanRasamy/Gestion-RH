-- À appliquer une seule fois sur une base déjà créée avec table.sql.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'detail_demande' AND column_name = 'motif')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'detail_demande' AND column_name = 'description') THEN
        ALTER TABLE detail_demande RENAME COLUMN motif TO description;
    END IF;
END $$;
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS id_employer INT REFERENCES employer(id);
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS motif TEXT;

CREATE TABLE IF NOT EXISTS demande_a_revoir(
    id SERIAL PRIMARY KEY,
    id_mpangataka INT NOT NULL REFERENCES employer(id),
    id_detail_demande INT NOT NULL REFERENCES detail_demande(id),
    etape_demandeuse VARCHAR(20) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    commentaire TEXT NOT NULL,
    nbr_personnel_propose INT NOT NULL DEFAULT 0,
    traite BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS offre(
    id SERIAL PRIMARY KEY,
    id_detail_demande INT NOT NULL UNIQUE REFERENCES detail_demande(id),
    date_limite DATE
);

CREATE TABLE IF NOT EXISTS grille_budget(
    id SERIAL PRIMARY KEY,
    id_poste INT NOT NULL REFERENCES poste(id),
    budget_min DOUBLE PRECISION NOT NULL,
    budget_max DOUBLE PRECISION NOT NULL,
    date_effective DATE NOT NULL,
    CHECK (budget_min <= budget_max)
);

CREATE TABLE IF NOT EXISTS parametre(
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    valeur DOUBLE PRECISION NOT NULL,
    date_effective DATE NOT NULL
);

INSERT INTO status(libelle)
SELECT valeur FROM (VALUES
    ('créé'), ('validé RH'), ('validé Finance'), ('validé DG'),
    ('à revoir'), ('refusé'), ('abandonné'), ('expiré')
) AS statuts(valeur)
WHERE NOT EXISTS (SELECT 1 FROM status WHERE lower(libelle) = lower(valeur));

INSERT INTO parametre(code, valeur, date_effective)
SELECT 'BUDGET', 0, CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM parametre WHERE code = 'BUDGET');

CREATE OR REPLACE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (h.id_demande)
    h.id_demande, h.id_status, s.libelle AS statut, h.id, h.date
FROM historique_demande_status h
JOIN status s ON s.id = h.id_status
ORDER BY h.id_demande, h.id DESC;
