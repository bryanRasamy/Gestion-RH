package gestion.entreprise.service;

import gestion.entreprise.modele.Employer;
import gestion.entreprise.modele.Poste;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.UtilisateurRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private UtilisateurRoleRepository utilisateurRepository;

    public int convertirId(String valeur) {
        if (valeur == null || valeur.isEmpty()) {
            throw new RuntimeException("Connectez-vous pour continuer");
        }
        int resultat = 0;
        for (int index = 0; index < valeur.length(); index++) {
            char chiffre = valeur.charAt(index);
            if (chiffre < '0' || chiffre > '9') {
                throw new RuntimeException("Session utilisateur invalide");
            }
            resultat = resultat * 10 + chiffre - '0';
        }
        return resultat;
    }

    public String roleUtilisateur(int idUtilisateur) {
        Utilisateur utilisateur = utilisateurRepository.findFirstById(idUtilisateur);
        if (utilisateur == null || utilisateur.getEmployer() == null) {
            return "NORMAL";
        }
        Employer employer = utilisateur.getEmployer();
        Poste poste = employer.getPoste();
        if (poste == null || poste.getDepartement() == null) {
            return "MANAGER";
        }
        String sigle = poste.getDepartement().getSigle();
        if ("rh".equalsIgnoreCase(sigle)) {
            return "RH";
        }
        if ("dg".equalsIgnoreCase(sigle)) {
            return "DG";
        }
        if ("fin".equalsIgnoreCase(sigle)) {
            return "FINANCE";
        }
        return "MANAGER";
    }

    public int employerConnecte(int idUtilisateur) {
        Utilisateur utilisateur = utilisateurRepository.findFirstById(idUtilisateur);
        if (utilisateur == null || utilisateur.getEmployer() == null) {
            throw new RuntimeException("Ce compte n'est pas rattaché à un employé");
        }
        return utilisateur.getEmployer().getId();
    }

    public void verifierRole(int idUtilisateur, String roleAttendu) {
        if ("DG".equals(roleUtilisateur(idUtilisateur))) {
            return;
        }
        if (!roleAttendu.equals(roleUtilisateur(idUtilisateur))) {
            throw new RuntimeException("Accès interdit pour votre rôle");
        }
    }

    public boolean estSuperAdmin(int idUtilisateur) {
        return "DG".equals(roleUtilisateur(idUtilisateur));
    }
}
