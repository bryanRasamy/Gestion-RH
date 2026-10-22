package gestion.entreprise.modele;

/** Roles applicatifs du circuit de validation : RH, Manager, Finance, DG. */
public enum TypeRole {
    RH("RH"),
    MANAGER("Manager"),
    FINANCE("Finance"),
    DG("DG");

    private final String libelle;

    TypeRole(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    public static TypeRole fromLibelle(String libelle) {
        if (libelle == null || libelle.isBlank()) {
            return null;
        }
        for (TypeRole role : values()) {
            if (role.libelle.equalsIgnoreCase(libelle.trim())) {
                return role;
            }
        }
        return null;
    }
}
