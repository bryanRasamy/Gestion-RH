-- À exécuter après table.sql, donnees.sql et 02_circuit_validation.sql.
-- Ajoute trois demandes de démonstration sans dupliquer les demandes existantes.

INSERT INTO demande(matricule, id_employer, date_limite)
SELECT donnees.matricule, employer.id, CURRENT_DATE + 30
FROM (VALUES
    ('DEM-TEST-001', 'EMP003'),
    ('DEM-TEST-002', 'EMP003'),
    ('DEM-TEST-003', 'EMP003')
) AS donnees(matricule, matricule_employer)
JOIN employer ON employer.matricule = donnees.matricule_employer
ON CONFLICT (matricule) DO NOTHING;

INSERT INTO detail_demande(id_demande, id_poste, id_departement, nbr_personnel, description)
SELECT demande.id, poste.id, departement.id, 2, donnees.description
FROM (VALUES
    ('DEM-TEST-001', 'DVS', 'IT', 'Renfort pour les projets en cours'),
    ('DEM-TEST-002', 'DVS', 'IT', 'Remplacement prévu au prochain trimestre'),
    ('DEM-TEST-003', 'DVS', 'IT', 'Renforcement de l’équipe de développement')
) AS donnees(matricule_demande, sigle_poste, sigle_departement, description)
JOIN demande ON demande.matricule = donnees.matricule_demande
JOIN poste ON poste.sigle = donnees.sigle_poste
JOIN departement ON departement.sigle = donnees.sigle_departement
WHERE NOT EXISTS (
    SELECT 1
    FROM detail_demande detail
    WHERE detail.id_demande = demande.id
);

INSERT INTO historique_demande_status(id_demande, id_status, id_employer, date)
SELECT demande.id, status.id, employer.id, CURRENT_DATE
FROM (VALUES
    ('DEM-TEST-001', 'créé', 'EMP003'),
    ('DEM-TEST-002', 'créé', 'EMP003'),
    ('DEM-TEST-002', 'validé RH', 'EMP002'),
    ('DEM-TEST-003', 'créé', 'EMP003'),
    ('DEM-TEST-003', 'validé RH', 'EMP002'),
    ('DEM-TEST-003', 'validé Finance', 'EMP009')
) AS donnees(matricule_demande, libelle_status, matricule_employer)
JOIN demande ON demande.matricule = donnees.matricule_demande
JOIN status ON lower(status.libelle) = lower(donnees.libelle_status)
JOIN employer ON employer.matricule = donnees.matricule_employer
WHERE NOT EXISTS (
    SELECT 1
    FROM historique_demande_status historique
    WHERE historique.id_demande = demande.id
      AND historique.id_status = status.id
);
