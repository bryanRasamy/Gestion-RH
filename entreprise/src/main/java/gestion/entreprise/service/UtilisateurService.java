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
        utilisateur.setRole(user.getRole());
    }

    public void saveUtilisateur(Utilisateur utilisateur) throws Exception{
        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur getById(int id) {
        return utilisateurRepository.findById(id).orElse(null);
    }

    public Utilisateur authentifierUtilisateur(HttpSession session, Model model) throws Exception{
        Utilisateur utilisateur = getById((int) session.getAttribute("idUtilisateur"));

        Employer employer=utilisateur.getEmployer();

        if (employer==null) {
            throw new RuntimeException("Utilisateur non autoriser. Seule les employer peuvent acceder à cette page");
        }

        Poste poste=employer.getPoste();

        if (!poste.getSigle().equalsIgnoreCase("MI") && !poste.getSigle().equalsIgnoreCase("MC") && !poste.getSigle().equalsIgnoreCase("DG")) {
            throw new RuntimeException("Seules les manager sont autoriser à acceder à cette page :"+poste.getSigle());
        }

        Departement departement=poste.getDepartement();

        if (departement.getSigle().equalsIgnoreCase("rh")) {
            throw new RuntimeException("Les rh ne sont pas autoriser à acceder à cette page");
        }

        model.addAttribute("utilisateur", utilisateur);
        model.addAttribute("poste", poste);
        model.addAttribute("departement", departement);


        return utilisateur;
    }
}
