package com.foodorderingapp.model.response;

import java.math.BigDecimal;
import java.util.UUID;

public class FoodResponse {
    private UUID id;
    private UUID shopId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;

    public FoodResponse() {}

    public FoodResponse(UUID id, UUID shopId, UUID categoryId, String categoryName, String name,
                        String description, BigDecimal price, String imageUrl, Boolean isAvailable) {
        this.id = id;
        this.shopId = shopId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
    }

    public UUID getId() { return id; }
    public UUID getShopId() { return shopId; }
    public UUID getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public Boolean getIsAvailable() { return isAvailable; }

    public void setId(UUID id) { this.id = id; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}