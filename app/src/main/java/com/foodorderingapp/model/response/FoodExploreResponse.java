package com.foodorderingapp.model.response;

public class FoodExploreResponse {
    private String id;
    private String foodName;
    private double price;
    private String foodImageUrl;
    private String description;
    private String shopId;
    private String shopName;
    private String categoryName;

    public String getId() { return id; }
    public String getFoodName() { return foodName; }
    public double getPrice() { return price; }
    public String getFoodImageUrl() { return foodImageUrl; }
    public String getDescription() { return description; }
    public String getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public String getCategoryName() { return categoryName; }
}
