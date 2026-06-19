package com.foodorderingapp.model.request;

public class CheckoutRequest {
    private String buildingName;
    private String dropOffPoint;
    private String buildingId;
    private String dropOffPointId;
    private String voucherCode;

    public CheckoutRequest(String buildingName, String dropOffPoint) {
        this.buildingName = buildingName;
        this.dropOffPoint = dropOffPoint;
    }

    public CheckoutRequest(String buildingName, String dropOffPoint,
                           String buildingId, String dropOffPointId, String voucherCode) {
        this.buildingName = buildingName;
        this.dropOffPoint = dropOffPoint;
        this.buildingId = buildingId;
        this.dropOffPointId = dropOffPointId;
        this.voucherCode = voucherCode;
    }
}
