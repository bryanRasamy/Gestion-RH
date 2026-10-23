-- =============================================================
--  BLOC 5 (minimal) : État de paie périodique
--  Insertion automatique à la validation du contrat par le DG.
-- =============================================================

CREATE TABLE paie_periode (
    id SERIAL PRIMARY KEY,
    id_employer INT NOT NULL,
    annee INT NOT NULL,
    mois INT NOT NULL CHECK (mois BETWEEN 1 AND 12),
    montant NUMERIC(15,2) NOT NULL,
    date_paiement TIMESTAMP NOT NULL DEFAULT NOW(),
    commentaire TEXT,
    FOREIGN KEY (id_employer) REFERENCES employer(id),
    UNIQUE (id_employer, annee, mois)
);

CREATE INDEX idx_paie_periode_employer ON paie_periode(id_employer);
CREATE INDEX idx_paie_periode_periode ON paie_periode(annee, mois);

