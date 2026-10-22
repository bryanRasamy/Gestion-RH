package gestion.entreprise.config;

import gestion.entreprise.modele.Utilisateur;
import gestion.entreprise.repository.UtilisateurRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

/** Resout le parametre annote {@link UtilisateurActif} a partir de la session. */
@Component
public class UtilisateurActifArgumentResolver implements HandlerMethodArgumentResolver {

    /** Cle de session contenant l'identifiant de l'utilisateur connecte. */
    public static final String ATTRIBUT_SESSION = "idUtilisateur";

    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurActifArgumentResolver(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UtilisateurActif.class)
                && Utilisateur.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                  NativeWebRequest request, WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
        HttpSession session = servletRequest != null ? servletRequest.getSession(false) : null;
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expiree.");
        }
        Object identifiant = session.getAttribute(ATTRIBUT_SESSION);
        if (!(identifiant instanceof Integer)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expiree.");
        }
        return utilisateurRepository.findById((Integer) identifiant)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expiree."));
    }
}
