package com.foodorderingapp.model.response;

public class CartItemResponse {
    private String id;
    private String foodId;
    private String foodName;
    private String foodImageUrl;
    private double price;
    private int quantity;
    private String note;

    public String getId() { return id; }
    public String getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public String getFoodImageUrl() { return foodImageUrl; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getNote() { return note; }
}
