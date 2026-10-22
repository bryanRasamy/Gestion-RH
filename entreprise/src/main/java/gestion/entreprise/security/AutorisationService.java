package gestion.entreprise.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.service.UtilisateurService;

@Service
public class AutorisationService {
  private final UtilisateurService utilisateurs;
  public AutorisationService(UtilisateurService utilisateurs) { this.utilisateurs=utilisateurs; }
  public Utilisateur exiger(HttpSession session, Role role) {
    Object id=session.getAttribute("idUtilisateur");
    if (!(id instanceof Integer)) throw new SecurityException("Veuillez vous connecter");
    Utilisateur u=utilisateurs.getById((Integer) id);
    if (u==null || u.getRole()!=role) throw new SecurityException("Vous n'êtes pas autorisé à effectuer cette action");
    return u;
  }
}
