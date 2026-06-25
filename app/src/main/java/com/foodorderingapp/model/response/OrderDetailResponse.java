package com.foodorderingapp.model.response;

public class OrderDetailResponse {
    private String foodName;
    private double price;
    private int quantity;

    public String getFoodName() { return foodName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    private String foodId;
    private String imageUrl;

    public String getFoodId() { return foodId; }
    public String getImageUrl() { return imageUrl; }

    public OrderDetailResponse() {}

    public OrderDetailResponse(String foodName, double price, int quantity, String foodId, String imageUrl) {
        this.foodName = foodName;
        this.price = price;
        this.quantity = quantity;
        this.foodId = foodId;
        this.imageUrl = imageUrl;
    }

    public void setFoodName(String foodName) { this.foodName = foodName; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setFoodId(String foodId) { this.foodId = foodId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
