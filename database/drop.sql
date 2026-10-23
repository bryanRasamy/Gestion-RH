-- =============================================================
--  PIVOT — Script de reset complet de la base
--  Supprime TOUTES les tables, vues, séquences du projet.
--  Ordre : dépendances d'abord, puis parents.
--  CASCADE évite d'avoir à respecter l'ordre strictement,
--  mais l'ordre est gardé par lisibilité.
-- =============================================================

-- =============================================================
-- BLOC 5 : Paie
-- =============================================================
DROP TABLE IF EXISTS paie_periode CASCADE;

-- =============================================================
-- BLOC 4 : Contrat, onboarding, badge, affectation, période
-- =============================================================
DROP TABLE IF EXISTS onboarding_tache CASCADE;
DROP TABLE IF EXISTS onboarding_tache_statut CASCADE;
DROP TABLE IF EXISTS onboarding_tache_type CASCADE;

DROP TABLE IF EXISTS badge CASCADE;
DROP TABLE IF EXISTS badge_statut CASCADE;

DROP TABLE IF EXISTS affectation CASCADE;
DROP TABLE IF EXISTS affectation_statut CASCADE;

-- Retirer la FK contrat_actuel avant de supprimer periode_emploi
ALTER TABLE periode_emploi DROP CONSTRAINT IF EXISTS fk_periode_contrat_actuel;

DROP TABLE IF EXISTS periode_emploi CASCADE;
DROP TABLE IF EXISTS periode_emploi_statut CASCADE;

DROP TABLE IF EXISTS historique_contrat_statut CASCADE;
DROP TABLE IF EXISTS contrat_statut CASCADE;

-- =============================================================
-- BLOC 3 : Proposition, contrat, paramètres, types
-- =============================================================
DROP TABLE IF EXISTS contrat CASCADE;
DROP TABLE IF EXISTS proposition_embauche_negociation CASCADE;
DROP TABLE IF EXISTS proposition_embauche_question CASCADE;
DROP TABLE IF EXISTS historique_proposition_embauche_statut CASCADE;
DROP TABLE IF EXISTS proposition_embauche CASCADE;
DROP TABLE IF EXISTS proposition_embauche_statut CASCADE;

DROP TABLE IF EXISTS reponse_type CASCADE;
DROP TABLE IF EXISTS contrat_type CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;

-- =============================================================
-- BLOC 2 : Tests techniques
-- =============================================================
DROP TABLE IF EXISTS test_technique_reponse CASCADE;
DROP TABLE IF EXISTS test_technique_passage CASCADE;
DROP TABLE IF EXISTS test_technique_question CASCADE;
DROP TABLE IF EXISTS test_technique CASCADE;
DROP TABLE IF EXISTS test_technique_niveau CASCADE;

DROP TABLE IF EXISTS historique_candidat_statut CASCADE;

-- =============================================================
-- FRONTOFFICE : Candidats
-- =============================================================
DROP TABLE IF EXISTS candidat CASCADE;

-- =============================================================
-- BACKOFFICE : Demandes, historiques, budgets
-- =============================================================
DROP TABLE IF EXISTS demande_a_revoir CASCADE;

DROP VIEW IF EXISTS v_historique_demande_status CASCADE;

DROP TABLE IF EXISTS historique_demande_status CASCADE;
DROP TABLE IF EXISTS detail_demande CASCADE;
DROP TABLE IF EXISTS demande CASCADE;

DROP TABLE IF EXISTS grille_budget CASCADE;
DROP TABLE IF EXISTS historique_budget_annuel CASCADE;

-- =============================================================
-- SOCLE : Utilisateurs, employés, postes, départements, rôles, statuts, diplômes
-- =============================================================
DROP TABLE IF EXISTS utilisateur CASCADE;
DROP TABLE IF EXISTS employer CASCADE;

DROP TABLE IF EXISTS poste CASCADE;
DROP TABLE IF EXISTS departement CASCADE;

DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS status CASCADE;
DROP TABLE IF EXISTS diplome CASCADE;

-- =============================================================
-- SÉQUENCES (normalement supprimées par CASCADE, mais on force)
-- =============================================================
DROP SEQUENCE IF EXISTS contrat_id_seq CASCADE;
DROP SEQUENCE IF EXISTS periode_emploi_id_seq CASCADE;
DROP SEQUENCE IF EXISTS affectation_id_seq CASCADE;
DROP SEQUENCE IF EXISTS badge_id_seq CASCADE;
DROP SEQUENCE IF EXISTS onboarding_tache_id_seq CASCADE;
DROP SEQUENCE IF EXISTS paie_periode_id_seq CASCADE;
DROP SEQUENCE IF EXISTS proposition_embauche_id_seq CASCADE;
DROP SEQUENCE IF EXISTS proposition_embauche_question_id_seq CASCADE;
DROP SEQUENCE IF EXISTS proposition_embauche_negociation_id_seq CASCADE;
DROP SEQUENCE IF EXISTS historique_proposition_embauche_statut_id_seq CASCADE;
DROP SEQUENCE IF EXISTS historique_contrat_statut_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test_technique_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test_technique_question_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test_technique_passage_id_seq CASCADE;
DROP SEQUENCE IF EXISTS test_technique_reponse_id_seq CASCADE;
DROP SEQUENCE IF EXISTS historique_candidat_statut_id_seq CASCADE;
DROP SEQUENCE IF EXISTS candidat_id_seq CASCADE;
DROP SEQUENCE IF EXISTS demande_id_seq CASCADE;
DROP SEQUENCE IF EXISTS detail_demande_id_seq CASCADE;
DROP SEQUENCE IF EXISTS historique_demande_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS demande_a_revoir_id_seq CASCADE;
DROP SEQUENCE IF EXISTS utilisateur_id_seq CASCADE;
DROP SEQUENCE IF EXISTS employer_id_seq CASCADE;
DROP SEQUENCE IF EXISTS poste_id_seq CASCADE;
DROP SEQUENCE IF EXISTS departement_id_seq CASCADE;
DROP SEQUENCE IF EXISTS role_id_seq CASCADE;
DROP SEQUENCE IF EXISTS status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS diplome_id_seq CASCADE;
DROP SEQUENCE IF EXISTS grille_budget_id_seq CASCADE;
DROP SEQUENCE IF EXISTS historique_budget_annuel_id_seq CASCADE;

-- =============================================================
-- EXTENSION pgcrypto (garde-la si tu vas réutiliser crypt()/gen_salt())
-- Commentée par défaut. Décommenter si tu veux vraiment tout supprimer.
-- =============================================================
-- DROP EXTENSION IF EXISTS pgcrypto CASCADE;

-- =============================================================
-- FIN
-- =============================================================