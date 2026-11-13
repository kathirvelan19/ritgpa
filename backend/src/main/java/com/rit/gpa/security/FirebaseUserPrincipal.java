package com.rit.gpa.security;

public class FirebaseUserPrincipal {
    private final String uid;
    private final String email;
    private final String name;

    public FirebaseUserPrincipal(String uid, String email, String name) {
        this.uid = uid;
        this.email = email;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}

