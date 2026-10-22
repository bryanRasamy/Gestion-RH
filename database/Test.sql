-- Script de test : ajout de nouvelles demandes sans supprimer celles déjà existantes.
-- Une offre est affichée dans /offres seulement si le dernier statut de la demande est "Valider" (sigle AU).

BEGIN;

-- 1) Ajouter une première demande de test (si elle n'existe pas)
INSERT INTO demande (matricule, id_employer, date_limite)
SELECT 'DM_TEST_001', 3, '2026-12-31'
WHERE NOT EXISTS (
    SELECT 1 FROM demande WHERE matricule = 'DM_TEST_001'
);

-- 2) Ajouter le détail de cette demande, si pas encore présent
INSERT INTO detail_demande (id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience)
SELECT d.id, 5, 3, 2, 'Test de validation pour page des offres', 2, 3
FROM demande d
WHERE d.matricule = 'DM_TEST_001'
  AND NOT EXISTS (
      SELECT 1
      FROM detail_demande dd
      WHERE dd.id_demande = d.id
  );

-- 3) Ajouter le statut initial "Creer" s'il n'est pas déjà présent
INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date)
SELECT d.id, s.id, d.id_employer, CURRENT_DATE
FROM demande d
JOIN status s ON s.sigle = 'CR'
WHERE d.matricule = 'DM_TEST_001'
  AND NOT EXISTS (
      SELECT 1
      FROM historique_demande_status h
      WHERE h.id_demande = d.id
        AND h.id_status = s.id
  );

-- 4) Ajouter le statut "Valider" pour la rendre visible dans la page /offres
INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date)
SELECT d.id, s.id, 2, CURRENT_DATE
FROM demande d
JOIN status s ON s.sigle = 'AU'
WHERE d.matricule = 'DM_TEST_001'
  AND NOT EXISTS (
      SELECT 1
      FROM historique_demande_status h
      WHERE h.id_demande = d.id
        AND h.id_status = s.id
  );

-- 5) Ajouter une autre demande de test pour "AJOUTER AUTRES"
INSERT INTO demande (matricule, id_employer, date_limite)
SELECT 'DM_TEST_002', 3, '2027-01-15'
WHERE NOT EXISTS (
    SELECT 1 FROM demande WHERE matricule = 'DM_TEST_002'
);

INSERT INTO detail_demande (id_demande, id_poste, id_departement, nbr_personnel, motif, id_diplome, annee_experience)
SELECT d.id, 4, 4, 3, 'Autre test pour validation des offres', 1, 2
FROM demande d
WHERE d.matricule = 'DM_TEST_002'
  AND NOT EXISTS (
      SELECT 1 FROM detail_demande dd WHERE dd.id_demande = d.id
  );

INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date)
SELECT d.id, s.id, d.id_employer, CURRENT_DATE
FROM demande d
JOIN status s ON s.sigle = 'CR'
WHERE d.matricule = 'DM_TEST_002'
  AND NOT EXISTS (
      SELECT 1 FROM historique_demande_status h WHERE h.id_demande = d.id AND h.id_status = s.id
  );

INSERT INTO historique_demande_status (id_demande, id_status, id_employer, date)
SELECT d.id, s.id, 2, CURRENT_DATE
FROM demande d
JOIN status s ON s.sigle = 'AU'
WHERE d.matricule = 'DM_TEST_002'
  AND NOT EXISTS (
      SELECT 1 FROM historique_demande_status h WHERE h.id_demande = d.id AND h.id_status = s.id
  );

-- 6) Vérification rapide
SELECT d.id,
       d.matricule,
       dd.id AS id_detail_demande,
       p.libelle AS poste,
       dep.libelle AS departement,
       s.libelle AS dernier_statut,
       d.date_limite
FROM demande d
JOIN detail_demande dd ON dd.id_demande = d.id
JOIN poste p ON p.id = dd.id_poste
JOIN departement dep ON dep.id = dd.id_departement
JOIN LATERAL (
    SELECT h.id_status, h.date
    FROM historique_demande_status h
    WHERE h.id_demande = d.id
    ORDER BY h.id DESC
    LIMIT 1
) last_h ON true
JOIN status s ON s.id = last_h.id_status
WHERE d.matricule IN ('DM_TEST_001', 'DM_TEST_002')
ORDER BY d.id;

COMMIT;
