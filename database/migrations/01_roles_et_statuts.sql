-- À exécuter une seule fois sur une base existante, après sauvegarde.
ALTER TABLE utilisateur ADD COLUMN IF NOT EXISTS role VARCHAR(20);
CREATE UNIQUE INDEX IF NOT EXISTS ux_departement_sigle ON departement(sigle);
UPDATE utilisateur SET role = CASE email
    WHEN 'dg@gmail.com' THEN 'DG'
    WHEN 'rh@gmail.com' THEN 'RH'
    WHEN 'manager@gmail.com' THEN 'MANAGER'
    ELSE 'CANDIDAT'
END WHERE role IS NULL;
ALTER TABLE utilisateur ALTER COLUMN role SET NOT NULL;
ALTER TABLE utilisateur ADD CONSTRAINT ck_utilisateur_role
    CHECK (role IN ('CANDIDAT', 'MANAGER', 'RH', 'FINANCE', 'DG'));

ALTER TABLE historique_demande_status ALTER COLUMN id_employer DROP NOT NULL;
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS etape VARCHAR(20);
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS commentaire TEXT;
ALTER TABLE historique_demande_status ADD COLUMN IF NOT EXISTS date_action TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
UPDATE historique_demande_status SET etape = 'CREATION' WHERE etape IS NULL;
ALTER TABLE historique_demande_status ALTER COLUMN etape SET NOT NULL;

ALTER TABLE detail_demande RENAME COLUMN motif TO description;
ALTER TABLE detail_demande ADD CONSTRAINT fk_detail_demande_diplome
    FOREIGN KEY (id_diplome) REFERENCES diplome(id);

INSERT INTO status(libelle, sigle)
SELECT x.libelle, x.sigle FROM (VALUES
 ('Créée', 'CREE'), ('Validée', 'VALIDE'), ('Refusée', 'REFUSE'),
 ('Abandonnée', 'ABANDONNE'), ('Expirée', 'EXPIRE')) AS x(libelle, sigle)
WHERE NOT EXISTS (SELECT 1 FROM status s WHERE s.sigle = x.sigle);
UPDATE historique_demande_status h SET id_status = s.id
FROM status s
WHERE s.sigle = CASE (SELECT old.sigle FROM status old WHERE old.id = h.id_status)
  WHEN 'CR' THEN 'CREE' WHEN 'AC' THEN 'VALIDE' WHEN 'AU' THEN 'VALIDE'
  WHEN 'RF' THEN 'REFUSE' WHEN 'AR' THEN 'VALIDE' WHEN 'AN' THEN 'ABANDONNE' END;
DELETE FROM status WHERE sigle IN ('CR', 'AC', 'AU', 'RF', 'AR', 'AN');
CREATE UNIQUE INDEX IF NOT EXISTS ux_status_sigle ON status(sigle);
CREATE INDEX IF NOT EXISTS ix_historique_demande_id ON historique_demande_status(id_demande, id DESC);
