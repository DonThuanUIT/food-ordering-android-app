package com.foodorderingapp.model.request;

public class UpdateProfileRequest {
    private final String fullName;
    private final String email;
    private final String buildingId;
    private final String avatarUrl;

    public UpdateProfileRequest(String fullName, String email, String buildingId, String avatarUrl) {
        this.fullName = fullName;
        this.email = email;
        this.buildingId = buildingId;
        this.avatarUrl = avatarUrl;
    }
}
