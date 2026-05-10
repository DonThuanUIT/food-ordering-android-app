package com.foodorderingapp.model.request;

public class BaseRegisterRequest {
    private String phone;
    private String password;
    private String fullName;
    private String email;

    public BaseRegisterRequest(String phone, String password, String fullName, String email) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    // Getters and Setters
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
}