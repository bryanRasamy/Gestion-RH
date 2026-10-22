package gestion.entreprise.dto;

/** Cotation Finance : OK, A_REVOIR ou NON par rapport au budget annuel. */
public enum EvaluationBudget {

    /** Le montant maximum tient dans le budget annuel. */
    OK("Validable", "Le montant maximum est dans le budget annuel."),

    /** Le maximum depasse le budget mais le minimum reste dans l'enveloppe. */
    A_REVOIR("A revoir", "Le montant maximum depasse le budget, mais le minimum reste dans l'enveloppe."),

    /** Le montant minimum depasse deja le budget annuel. */
    NON("Non validable", "Le montant minimum depasse deja le budget annuel.");

    private final String libelle;
    private final String message;

    EvaluationBudget(String libelle, String message) {
        this.libelle = libelle;
        this.message = message;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getMessage() {
        return message;
    }
}
