package com.foodorderingapp.model.request;

public class ResendOtpRequest {
    private String email;
    public ResendOtpRequest(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
}
