package com.foodorderingapp.model.request;

import java.util.List;

public class CheckoutRequest {
    private String shopId;
    private List<String> cartItemIds;
    private String paymentMethod;
    private String buildingId;
    private String dropOffPointId;
    private String voucherCode;
    private String note;

    public CheckoutRequest(String shopId, List<String> cartItemIds, String paymentMethod,
                           String buildingId, String dropOffPointId, String voucherCode,
                           String note) {
        this.shopId = shopId;
        this.cartItemIds = cartItemIds;
        this.paymentMethod = paymentMethod;
        this.buildingId = buildingId;
        this.dropOffPointId = dropOffPointId;
        this.voucherCode = voucherCode;
        this.note = note;
    }
}
