package com.foodorderingapp.model.response;

public class BuildingResponse {
    private String id;
    private String name;
    private Double latitude;
    private Double longitude;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
