package com.sante.lims.model;

public class CustomerProfile {
    private long id;
    private String fullName;
    private String email;
    private boolean emailVerified;

    public CustomerProfile(long id, String fullName, String email, boolean emailVerified) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
