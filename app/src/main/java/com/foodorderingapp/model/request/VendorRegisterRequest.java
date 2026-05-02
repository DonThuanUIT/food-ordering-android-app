package com.foodorderingapp.model.request;

public class VendorRegisterRequest extends BaseRegisterRequest {
    private String shopName;
    private String description;
    private String openTime;
    private String closeTime;

    public VendorRegisterRequest(String phone, String password, String fullName, String email,
                                 String shopName, String description, String openTime, String closeTime) {
        super(phone, password, fullName, email);
        this.shopName = shopName;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getShopName() { return shopName; }
}