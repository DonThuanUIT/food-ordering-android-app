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

    // Constructor for generic registration (used in RegisterViewModel)
    public RegisterRequest(String phone, String password, String fullName) {
        this.phone = phone;
        this.password = password;
        this.fullName = fullName;
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

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }
}
