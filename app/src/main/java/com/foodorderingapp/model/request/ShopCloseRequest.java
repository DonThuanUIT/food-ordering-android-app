package com.foodorderingapp.model.request;

public class ShopCloseRequest {
    private String verificationType;
    private String password;
    private String otpCode;

    public ShopCloseRequest(String verificationType, String password, String otpCode) {
        this.verificationType = verificationType;
        this.password = password;
        this.otpCode = otpCode;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public void setVerificationType(String verificationType) {
        this.verificationType = verificationType;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
