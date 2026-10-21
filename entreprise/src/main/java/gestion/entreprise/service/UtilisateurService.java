package gestion.entreprise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gestion.entreprise.modele.*;
import gestion.entreprise.repository.*;

@Service
public class UtilisateurService {

    @Autowired 
    private UtilisateurRepository utilisateurRepository;

    @Autowired 
    private PasswordEncoder passwordEncoder;

    public void findUtilisateur(Utilisateur utilisateur) throws Exception{
        Utilisateur user = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (user==null) {
            throw new Exception("L'email est invalide");
        }

        if(!passwordEncoder.matches(utilisateur.getMdp(), user.getMdp())){
            throw new Exception("Le mot de passe est invalide");
        }

        utilisateur.setId(user.getId());
        utilisateur.setEmployer(user.getEmployer());
    }

    public void saveUtilisateur(Utilisateur utilisateur) throws Exception{
        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur getById(int id) throws Exception{
        return utilisateurRepository.findById(id).orElse(null);
    }
}