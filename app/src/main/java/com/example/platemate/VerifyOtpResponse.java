package com.example.platemate;

public class VerifyOtpResponse {
    private boolean valid;
    private String message;

    public VerifyOtpResponse() {}

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

