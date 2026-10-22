-- Ajoute l'option permettant de proposer un effectif nul pendant une révision.
ALTER TABLE demande_a_revoir
    ADD COLUMN IF NOT EXISTS annuler_poste BOOLEAN NOT NULL DEFAULT FALSE;
