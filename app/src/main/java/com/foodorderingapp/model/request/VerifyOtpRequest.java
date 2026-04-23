package com.foodorderingapp.model.request;

public class VerifyOtpRequest {
    private String email;
    private String otpCode;

    public VerifyOtpRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() {
        return email;
    }

    public String getOtpCode() {
        return otpCode;
    }
}