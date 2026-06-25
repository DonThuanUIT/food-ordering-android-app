package com.foodorderingapp.model.response;

import java.util.UUID;

public class TopProductData {
    private UUID foodId;
    private String foodName;
    private Long quantitySold;
    private Long revenue;

    public TopProductData() {}

    public TopProductData(UUID foodId, String foodName, Long quantitySold, Long revenue) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public UUID getFoodId() {
        return foodId;
    }

    public void setFoodId(UUID foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Long getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(Long quantitySold) {
        this.quantitySold = quantitySold;
    }

    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }
}
