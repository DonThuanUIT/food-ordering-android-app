package com.foodorderingapp.model.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class FoodRequest {
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private List<String> tags;
    private String cuisine;
    private Integer spicyLevel;

    public FoodRequest(UUID categoryId, String name, String description, BigDecimal price, String imageUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public FoodRequest(UUID categoryId, String name, String description, BigDecimal price, String imageUrl,
                       List<String> tags, String cuisine, Integer spicyLevel) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.cuisine = cuisine;
        this.spicyLevel = spicyLevel;
    }

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public Integer getSpicyLevel() { return spicyLevel; }
    public void setSpicyLevel(Integer spicyLevel) { this.spicyLevel = spicyLevel; }
}