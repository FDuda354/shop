package pl.dudios.shopmvn.security.user.model;

public enum Role {
    ROLE_USER("USER"),
    ROLE_ADMIN("ADMIN");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    public String getName() {
        return role;
    }
}
