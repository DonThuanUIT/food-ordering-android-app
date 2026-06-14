package com.foodorderingapp.model.response;

public class OrderDetailResponse {
    private String foodName;
    private double price;
    private int quantity;

    public String getFoodName() { return foodName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
}
