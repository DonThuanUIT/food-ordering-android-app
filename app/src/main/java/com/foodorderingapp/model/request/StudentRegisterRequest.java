package com.foodorderingapp.model.request;

public class StudentRegisterRequest extends BaseRegisterRequest {
    private String buildingId;

    public StudentRegisterRequest(String phone, String password, String fullName, String email, String buildingId) {
        super(phone, password, fullName, email);
        this.buildingId = buildingId;
    }

    public String getBuildingId() { return buildingId; }
}