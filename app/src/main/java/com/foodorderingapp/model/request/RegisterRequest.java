package com.foodorderingapp.model.request;

public class RegisterRequest {
    private String phone;
    private String password;
    private String fullName;

    public RegisterRequest(String phone, String password, String fullName) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }
}
