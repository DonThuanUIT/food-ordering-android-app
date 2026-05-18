package com.foodorderingapp.model.request;

public class AddToCartRequest {
    private String foodId;
    private int quantity;

    public AddToCartRequest(String foodId, int quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public String getFoodId() {
        return foodId;
    }

    public int getQuantity() {
        return quantity;
    }
}
