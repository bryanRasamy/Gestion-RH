package gestion.entreprise.service;

import gestion.entreprise.modele.Utilisateur;
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

    public Utilisateur authentifier(String email, String mdp) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur == null || !passwordEncoder.matches(mdp, utilisateur.getMdp())) {
            throw new RuntimeException("Email ou mot de passe invalide");
        }
        return utilisateur;
    }

    public Utilisateur inscrire(String email, String mdp) {
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (mdp == null || mdp.length() < 8) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 8 caractères");
        }
        if (utilisateurRepository.findByEmail(email) != null) {
            throw new RuntimeException("Cette adresse email est déjà utilisée");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email.trim());
        utilisateur.setMdp(passwordEncoder.encode(mdp));
        return utilisateurRepository.save(utilisateur);
    }
}
