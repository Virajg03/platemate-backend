package com.example.platemate;

public class VerifyOtpRequest {
    private String username;
    private String email;
    private String otp;

    public VerifyOtpRequest() {}

    public VerifyOtpRequest(String username, String email, String otp) {
        this.username = username;
        this.email = email;
        this.otp = otp;
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

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

