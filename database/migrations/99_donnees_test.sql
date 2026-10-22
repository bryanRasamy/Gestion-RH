-- Compte de démonstration Finance : finance@gmail.com / finance
WITH dep AS (
  INSERT INTO departement(libelle, sigle) VALUES ('Finance', 'FIN')
  ON CONFLICT (sigle) DO UPDATE SET libelle = EXCLUDED.libelle RETURNING id
), poste_fin AS (
  INSERT INTO poste(id_departement, libelle, sigle, salaire)
  SELECT id, 'Responsable Finance', 'RFN', 5000000 FROM dep
  RETURNING id
), emp AS (
  INSERT INTO employer(matricule, nom, prenom, compte, id_poste, salaire)
  SELECT 'EMP009', 'Finance', 'Responsable', 'CP009', id, 5000000 FROM poste_fin
  ON CONFLICT (matricule) DO UPDATE SET id_poste = EXCLUDED.id_poste RETURNING id
)
INSERT INTO utilisateur(id_employer, email, mdp, role)
SELECT id, 'finance@gmail.com', crypt('finance', gen_salt('bf', 10)), 'FINANCE' FROM emp
ON CONFLICT (email) DO UPDATE SET role = 'FINANCE', id_employer = EXCLUDED.id_employer;
