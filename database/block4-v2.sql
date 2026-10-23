-- =============================================================
--  BLOC 4 v2 : Lien explicite contrat actuel sur periode_emploi
-- =============================================================


-- 1. Ajouter la colonne contrat actuel sur periode_emploi
ALTER TABLE periode_emploi ADD COLUMN IF NOT EXISTS id_contrat_actuel INT;

-- 2. Contrainte FK (nullable — remplie à la validation DG)
ALTER TABLE periode_emploi
    ADD CONSTRAINT fk_periode_contrat_actuel
    FOREIGN KEY (id_contrat_actuel) REFERENCES contrat(id);

-- 3. Backfill : pour les périodes existantes, prendre le contrat le plus récent
UPDATE periode_emploi pe
SET id_contrat_actuel = (
    SELECT c.id
    FROM affectation a
    JOIN contrat c ON c.id = a.id_contrat
    WHERE a.id_periode_emploi = pe.id
    ORDER BY c.id DESC
    LIMIT 1
)
WHERE pe.id_contrat_actuel IS NULL;

-- 4. Retirer la contrainte UNIQUE sur contrat.id_proposition_embauche
--    (déjà fait si tu as appliqué le fix précédent, mais au cas où)
ALTER TABLE contrat DROP CONSTRAINT IF EXISTS contrat_id_proposition_embauche_key;