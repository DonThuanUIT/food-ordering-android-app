package com.foodorderingapp.model.request;

import java.util.List;

public class CheckoutRequest {
    private String shopId;
    private List<String> cartItemIds;
    private String buildingId;
    private String voucherCode;
    private String note;

    public CheckoutRequest(String shopId, List<String> cartItemIds, String buildingId,
                           String voucherCode, String note) {
        this.shopId = shopId;
        this.cartItemIds = cartItemIds;
        this.buildingId = buildingId;
        this.voucherCode = voucherCode;
        this.note = note;
    }
}
