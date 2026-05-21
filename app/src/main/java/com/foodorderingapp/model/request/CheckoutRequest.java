package com.foodorderingapp.model.request;

public class CheckoutRequest {
    private String buildingName;
    private String dropOffPoint;

    public CheckoutRequest(String buildingName, String dropOffPoint) {
        this.buildingName = buildingName;
        this.dropOffPoint = dropOffPoint;
    }
}
