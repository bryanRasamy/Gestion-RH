CREATE TABLE demande_a_revoir (
  id SERIAL PRIMARY KEY, id_demande INT NOT NULL REFERENCES demande(id),
  id_detail_demande INT NOT NULL REFERENCES detail_demande(id),
  id_employer_demandeur INT NOT NULL REFERENCES employer(id),
  etape VARCHAR(20) NOT NULL, commentaire TEXT NOT NULL,
  nbr_personnel_propose INT, date_action TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  traite BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE offre (
  id SERIAL PRIMARY KEY, id_detail_demande INT NOT NULL UNIQUE REFERENCES detail_demande(id),
  date_creation TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE grille_budget (
  id SERIAL PRIMARY KEY, id_poste INT NOT NULL REFERENCES poste(id),
  budget_min NUMERIC(14,2) NOT NULL, budget_max NUMERIC(14,2) NOT NULL,
  date_effective DATE NOT NULL, CHECK (budget_min >= 0 AND budget_max >= budget_min)
);
CREATE TABLE parametre (code VARCHAR(40) PRIMARY KEY, valeur NUMERIC(14,2) NOT NULL);
INSERT INTO parametre(code, valeur) VALUES ('BUDGET', 0) ON CONFLICT (code) DO NOTHING;

CREATE OR REPLACE VIEW v_demande_statut_courant AS
SELECT DISTINCT ON (h.id_demande) h.id, h.id_demande, h.id_status, h.id_employer,
  h.etape, h.commentaire, h.date_action
FROM historique_demande_status h ORDER BY h.id_demande, h.id DESC;
