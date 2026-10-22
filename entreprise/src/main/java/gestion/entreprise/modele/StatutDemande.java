package gestion.entreprise.modele;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * Cycle de vie d'une demande.
 *
 * <p>Source unique des libelles de statut manipules par le circuit de
 * validation : Manager (creation) -> RH -> Finance -> DG, avec les issues
 * {@link #A_REVOIR}, {@link #REFUSER}, {@link #ABANDONNE} et {@link #EXPIRE}.</p>
 */
public enum StatutDemande {

    /** Demande creee par le manager, en attente de la RH. */
    CREE("cree", "CREE"),

    /** Validee par la RH, en attente de la Finance. */
    VALIDER_RH("valider rh", "VALIDER_RH"),

    /** Validee par la Finance, en attente de la DG. */
    VALIDER_FINANCE("valider finance", "VALIDER_FINANCE"),

    /** Validee par la DG : etat final. */
    VALIDER_DG("valider dg", "VALIDER_DG"),

    /** Renvoyee au manager pour correction. */
    A_REVOIR("a revoir", "A_REVOIR"),

    /** Explicitement refusee : etat final. */
    REFUSER("refuser", "REFUSER"),

    /** Abandonnee par le manager : etat final. */
    ABANDONNE("abandone", "ABANDONNE"),

    /** Date limite depassee : etat final. */
    EXPIRE("expire", "EXPIRE");

    private final String libelle;
    private final String sigle;

    StatutDemande(String libelle, String sigle) {
        this.libelle = libelle;
        this.sigle = sigle;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getSigle() {
        return sigle;
    }

    /** Statuts vers lesquels une demande peut passer depuis cet etat. */
    public Set<StatutDemande> transitionsAutorisees() {
        return switch (this) {
            case CREE -> EnumSet.of(VALIDER_RH, A_REVOIR, REFUSER, EXPIRE);
            case VALIDER_RH -> EnumSet.of(VALIDER_FINANCE, A_REVOIR, REFUSER, EXPIRE);
            case VALIDER_FINANCE -> EnumSet.of(VALIDER_DG, A_REVOIR, REFUSER, EXPIRE);
            case A_REVOIR -> EnumSet.of(VALIDER_RH, VALIDER_FINANCE, VALIDER_DG, ABANDONNE, EXPIRE);
            case VALIDER_DG, REFUSER, ABANDONNE, EXPIRE -> EnumSet.noneOf(StatutDemande.class);
        };
    }

    /** Un etat final ne peut plus evoluer. */
    public boolean estFinal() {
        return transitionsAutorisees().isEmpty();
    }

    /** Les demandes portant ce statut peuvent expirer au dela de leur echeance. */
    public boolean estExpirable() {
        return this == CREE || this == VALIDER_RH || this == VALIDER_FINANCE || this == A_REVOIR;
    }

    /** Statut attendu par un role pour agir sur une demande. */
    public static StatutDemande statutAttendu(TypeRole role) {
        return switch (role) {
            case RH -> CREE;
            case FINANCE -> VALIDER_RH;
            case DG -> VALIDER_FINANCE;
            case MANAGER -> A_REVOIR;
        };
    }

    /** Statut a appliquer quand un role valide une demande. */
    public static StatutDemande statutValidePar(TypeRole role) {
        return switch (role) {
            case RH -> VALIDER_RH;
            case FINANCE -> VALIDER_FINANCE;
            case DG -> VALIDER_DG;
            case MANAGER -> null;
        };
    }

    /** Retrouve un statut a partir de son libelle (insensible a la casse). */
    public static StatutDemande depuisLibelle(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            return null;
        }
        String normalise = libelle.trim().toLowerCase(Locale.ROOT);
        for (StatutDemande statut : values()) {
            if (statut.libelle.equals(normalise) || statut.sigle.toLowerCase(Locale.ROOT).equals(normalise)) {
                return statut;
            }
        }
        return null;
    }
}
