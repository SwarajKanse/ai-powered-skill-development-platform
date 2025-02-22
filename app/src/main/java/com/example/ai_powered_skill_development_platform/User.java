package com.example.ai_powered_skill_development_platform;

public class User {
    private String email;

    public User() {
        // Required empty constructor for Firestore
    }

    public User(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
