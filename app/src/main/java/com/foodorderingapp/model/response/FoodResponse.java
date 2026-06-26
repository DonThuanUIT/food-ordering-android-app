package com.foodorderingapp.model.response;

import java.math.BigDecimal;
import java.util.List;
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
    private List<String> tags;
    private String cuisine;
    private Integer spicyLevel;
    private Integer soldCount;

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

    public FoodResponse(UUID id, UUID shopId, UUID categoryId, String categoryName, String name,
                        String description, BigDecimal price, String imageUrl, Boolean isAvailable,
                        List<String> tags, String cuisine, Integer spicyLevel) {
        this.id = id;
        this.shopId = shopId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.tags = tags;
        this.cuisine = cuisine;
        this.spicyLevel = spicyLevel;
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
    public List<String> getTags() { return tags; }
    public String getCuisine() { return cuisine; }
    public Integer getSpicyLevel() { return spicyLevel; }

    public void setId(UUID id) { this.id = id; }
    public void setShopId(UUID shopId) { this.shopId = shopId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setSpicyLevel(Integer spicyLevel) { this.spicyLevel = spicyLevel; }
    public Integer getSoldCount() { return soldCount != null ? soldCount : 0; }
    public void setSoldCount(Integer soldCount) { this.soldCount = soldCount; }
}