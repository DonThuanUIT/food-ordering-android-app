package com.foodorderingapp.model.request;
public class RegisterRequest {
    private String phone;
    private String password;
    private String fullName;
    private String email;

    // Student
    private String buildingId;

    // Vendor
    private String shopName;
    private String description;
    private String openTime;
    private String closeTime;

    public RegisterRequest() {
    }

    // Student register
    public RegisterRequest(String phone, String password, String fullName, String email, String buildingId) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.buildingId = buildingId;
    }

    // Vendor register
    public RegisterRequest(String phone, String password, String fullName, String email,
                           String shopName, String description, String openTime, String closeTime) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.shopName = shopName;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
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

    public String getEmail() {
        return email;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getShopName() {
        return shopName;
    }

    public String getDescription() {
        return description;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }
}