package com.foodorderingapp.model.request;

import java.math.BigDecimal;
import java.util.UUID;

public class FoodRequest {
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;

    public FoodRequest(UUID categoryId, String name, String description, BigDecimal price, String imageUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    // Getter/Setter...
}