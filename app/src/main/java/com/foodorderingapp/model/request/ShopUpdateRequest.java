package com.foodorderingapp.model.request;

public class ShopUpdateRequest {
    private String name;
    private String address;
    private String description;
    private String openTime; // Represented as "HH:mm" formatted String
    private String closeTime; // Represented as "HH:mm" formatted String

    public ShopUpdateRequest() {}

    public ShopUpdateRequest(String name, String address, String description, String openTime, String closeTime) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
