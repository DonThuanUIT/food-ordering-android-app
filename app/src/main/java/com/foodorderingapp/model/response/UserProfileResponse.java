package com.foodorderingapp.model.response;

public class UserProfileResponse {
    private String id;
    private String phone;
    private String fullName;
    private String email;
    private String role;
    private String buildingId;
    private String buildingName;
    private String avatarUrl;

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
