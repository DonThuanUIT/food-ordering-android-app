package com.foodorderingapp.model.response;

public class BuildingResponse {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name == null ? "" : name;
    }
}
