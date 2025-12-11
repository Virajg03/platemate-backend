package com.example.platemate;

public class ResendOtpRequest {
    private String username;
    private String email;

    public ResendOtpRequest() {}

    public ResendOtpRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

