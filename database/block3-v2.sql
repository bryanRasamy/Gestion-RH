-- =============================================================
--  BLOC 3 v2 : Contrat + corrections
-- =============================================================

-- 1. Ajouter les caractéristiques aux types de contrat
ALTER TABLE contrat_type ADD COLUMN IF NOT EXISTS duree_max_mois INT;
ALTER TABLE contrat_type ADD COLUMN IF NOT EXISTS nb_renouvellement_max INT DEFAULT 0;

UPDATE contrat_type SET duree_max_mois = 6,  nb_renouvellement_max = 1 WHERE libelle = 'CE';
UPDATE contrat_type SET duree_max_mois = 24, nb_renouvellement_max = 1 WHERE libelle = 'CDD';
UPDATE contrat_type SET duree_max_mois = NULL, nb_renouvellement_max = 0 WHERE libelle = 'CDI';

-- 2. Ajouter l'état EN_ATTENTE_FINAL (candidat confirme après négociation RH)
INSERT INTO proposition_embauche_statut (libelle)
VALUES ('EN_ATTENTE_FINAL')
ON CONFLICT (libelle) DO NOTHING;

-- 3. Table contrat (créée lors de l'acceptation)
CREATE TABLE IF NOT EXISTS contrat (
    id SERIAL PRIMARY KEY,
    id_proposition_embauche INT NOT NULL UNIQUE,
    id_contrat_type INT NOT NULL,
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP,
    salaire NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_proposition_embauche) REFERENCES proposition_embauche(id),
    FOREIGN KEY (id_contrat_type) REFERENCES contrat_type(id)
);

-- 4. Nettoyer les questions 5 et 6 des propositions existantes (si déjà créées)
DELETE FROM proposition_embauche_question
WHERE ordre IN (5, 6)
  AND id_proposition_embauche IN (
    SELECT id FROM proposition_embauche WHERE id_proposition_embauche_statut IN (
      SELECT id FROM proposition_embauche_statut WHERE libelle = 'EN_ATTENTE_CANDIDAT'
    )
  );