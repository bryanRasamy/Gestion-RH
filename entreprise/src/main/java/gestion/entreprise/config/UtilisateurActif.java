package gestion.entreprise.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injecte dans la methode l'utilisateur connecte (session {@code idUtilisateur}).
 * Lève une erreur 401 si aucune session n'est ouverte.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UtilisateurActif {
}
