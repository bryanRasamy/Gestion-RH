package gestion.entreprise.service;

import gestion.entreprise.modele.Employer;
import gestion.entreprise.modele.Role;
import gestion.entreprise.modele.TypeRole;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.RoleRepository;
import gestion.entreprise.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public void findUtilisateur(Utilisateur utilisateur) throws Exception {
        Utilisateur user = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (user == null) {
            throw new Exception("L'email est invalide");
        }

        if (!passwordEncoder.matches(utilisateur.getMdp(), user.getMdp())) {
            throw new Exception("Le mot de passe est invalide");
        }

        utilisateur.setId(user.getId());
        utilisateur.setEmployer(user.getEmployer());
        utilisateur.setIdRole(user.getIdRole());
    }

    public void saveUtilisateur(Utilisateur utilisateur) throws Exception {
        utilisateurRepository.save(utilisateur);
    }

    /** Role applicatif de l'utilisateur, {@code null} s'il n'en a pas. */
    public TypeRole role(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getIdRole() <= 0) {
            return null;
        }
        Role role = roleRepository.findOneById(utilisateur.getIdRole());
        return role != null ? TypeRole.fromLibelle(role.getLibelle()) : null;
    }

    /** Libelle du role de l'utilisateur ("RH", "Finance", "DG", "Manager"). */
    public String libelleRole(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getIdRole() <= 0) {
            return null;
        }
        Role role = roleRepository.findOneById(utilisateur.getIdRole());
        return role != null ? role.getLibelle() : null;
    }

    /** Identifiant de l'employe associe a l'utilisateur, 0 s'il n'y en a pas. */
    public int idEmploye(Utilisateur utilisateur) {
        Employer employer = utilisateur != null ? utilisateur.getEmployer() : null;
        return employer != null ? employer.getId() : 0;
    }

    /** Nom affiche de l'employe connecte. */
    public String nomAffiche(Utilisateur utilisateur) {
        Employer employer = utilisateur != null ? utilisateur.getEmployer() : null;
        if (employer == null) {
            return utilisateur != null ? utilisateur.getEmail() : "";
        }
        return employer.getPrenom() + " " + employer.getNom();
    }
}
