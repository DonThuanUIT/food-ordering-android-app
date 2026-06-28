package com.foodorderingapp.model.response;

import com.google.gson.annotations.SerializedName;

public class ShopResponse {
    private String id;
    private String ownerId;
    private String name;
    private String description;
    private String openTime;
    private String closeTime;
    private String address;
    private String status;
    private String coverUrl;
    private String logoUrl;
    private Boolean isOpen;
    private Boolean currentlyOpen;
    private String email;
    private String phone;

    @SerializedName(value = "isActive", alternate = {"active"})
    private Boolean isActive;
    private String displayStatus;

    private String bankName;
    private String bankAccountNumber;
    private String bankAccountOwner;
    private Boolean orderAlertsEnabled;
    private Boolean dormPromotionsEnabled;
    private Boolean turboModeEnabled;

    private String monFriOpenTime;
    private String monFriCloseTime;
    private String satOpenTime;
    private String satCloseTime;
    private String sunOpenTime;
    private String sunCloseTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive != null ? isActive : false;
    }

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public Boolean getCurrentlyOpen() {
        return currentlyOpen;
    }

    public void setCurrentlyOpen(Boolean currentlyOpen) {
        this.currentlyOpen = currentlyOpen;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountOwner() {
        return bankAccountOwner;
    }

    public void setBankAccountOwner(String bankAccountOwner) {
        this.bankAccountOwner = bankAccountOwner;
    }

    public Boolean getOrderAlertsEnabled() {
        return orderAlertsEnabled;
    }

    public void setOrderAlertsEnabled(Boolean orderAlertsEnabled) {
        this.orderAlertsEnabled = orderAlertsEnabled;
    }

    public Boolean getDormPromotionsEnabled() {
        return dormPromotionsEnabled;
    }

    public void setDormPromotionsEnabled(Boolean dormPromotionsEnabled) {
        this.dormPromotionsEnabled = dormPromotionsEnabled;
    }

    public Boolean getTurboModeEnabled() {
        return turboModeEnabled;
    }

    public void setTurboModeEnabled(Boolean turboModeEnabled) {
        this.turboModeEnabled = turboModeEnabled;
    }

    public String getMonFriOpenTime() {
        return monFriOpenTime;
    }

    public void setMonFriOpenTime(String monFriOpenTime) {
        this.monFriOpenTime = monFriOpenTime;
    }

    public String getMonFriCloseTime() {
        return monFriCloseTime;
    }

    public void setMonFriCloseTime(String monFriCloseTime) {
        this.monFriCloseTime = monFriCloseTime;
    }

    public String getSatOpenTime() {
        return satOpenTime;
    }

    public void setSatOpenTime(String satOpenTime) {
        this.satOpenTime = satOpenTime;
    }

    public String getSatCloseTime() {
        return satCloseTime;
    }

    public void setSatCloseTime(String satCloseTime) {
        this.satCloseTime = satCloseTime;
    }

    public String getSunOpenTime() {
        return sunOpenTime;
    }

    public void setSunOpenTime(String sunOpenTime) {
        this.sunOpenTime = sunOpenTime;
    }

    public String getSunCloseTime() {
        return sunCloseTime;
    }

    public void setSunCloseTime(String sunCloseTime) {
        this.sunCloseTime = sunCloseTime;
    }

    private Double latitude;
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    private Double rating;

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
