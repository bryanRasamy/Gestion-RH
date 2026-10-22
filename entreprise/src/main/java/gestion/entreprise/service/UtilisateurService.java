package gestion.entreprise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.ui.Model;
import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;
import jakarta.servlet.http.HttpSession;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authentifie un utilisateur à partir de son email + mot de passe.
     * Remplit les champs id, employer et role de l'objet passé en paramètre.
     * (Utilisé par LoginController.seConnecter)
     */
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
        utilisateur.setRole(user.getRole());
    }

    public void saveUtilisateur(Utilisateur utilisateur) throws Exception {
        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur getById(int id) throws Exception {
        return utilisateurRepository.findById(id).orElse(null);
    }

    /**
     * Frontoffice : retourne l'utilisateur seulement s'il n'a pas d'employer
     * (donc s'il est candidat, pas employé).
     */
    public Utilisateur getCandidatById(int id) throws Exception {
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);

        if (utilisateur == null) {
            throw new Exception("Utilisateur introuvable");
        }

        if (utilisateur.getEmployer() != null) {
            throw new Exception("Cet espace est réservé aux candidats");
        }

        return utilisateur;
    }

    /**
     * UNE seule méthode d'authentification.
     * Récupère l'utilisateur en session, ajoute les attributs au model si model != null.
     * Le contrôle de rôle se fait dans le controller appelant avec if/else.
     */
    public Utilisateur authentifierUtilisateur(HttpSession session, Model model) throws Exception {
        Object sessionAttr = session.getAttribute("idUtilisateur");
        if (sessionAttr == null) {
            throw new RuntimeException("Session expirée. Veuillez vous reconnecter.");
        }

        Utilisateur utilisateur = getById((int) sessionAttr);

        if (utilisateur == null) {
            throw new RuntimeException("Utilisateur introuvable.");
        }

        if (model != null) {
            model.addAttribute("utilisateur", utilisateur);

            if (utilisateur.getRole() != null) {
                model.addAttribute("role", utilisateur.getRole());
            }

            if (utilisateur.getEmployer() != null) {
                Employer employer = utilisateur.getEmployer();
                model.addAttribute("employer", employer);
                if (employer.getPoste() != null) {
                    model.addAttribute("poste", employer.getPoste());
                    if (employer.getPoste().getDepartement() != null) {
                        model.addAttribute("departement", employer.getPoste().getDepartement());
                    }
                }
            }
        }

        return utilisateur;
    }
}