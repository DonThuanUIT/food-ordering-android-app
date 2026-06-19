package com.foodorderingapp.model.response;

public class DropOffPointResponse {
    private String id;
    private String name;
    private String buildingId;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBuildingId() {
        return buildingId;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
