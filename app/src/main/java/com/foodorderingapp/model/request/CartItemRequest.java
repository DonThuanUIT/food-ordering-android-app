package com.foodorderingapp.model.request;

public class CartItemRequest {
    private String foodId;
    private int quantity;
    private String note;

    public CartItemRequest(String foodId, int quantity, String note) {
        this.foodId = foodId;
        this.quantity = quantity;
        this.note = note;
    }
}
