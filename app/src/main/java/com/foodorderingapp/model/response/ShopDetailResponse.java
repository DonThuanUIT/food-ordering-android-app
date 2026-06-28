package com.foodorderingapp.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ShopDetailResponse {
    private String id;
    private String name;
    private String address;
    private String description;
    private String coverUrl;
    private String logoUrl;
    private String openTime;
    private String closeTime;
    private Boolean isOpen;
    private Boolean currentlyOpen;
    private Double latitude;
    private Double longitude;
    private List<CategoryMenu> menu;

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getOpenTime() {
        return openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public Boolean getCurrentlyOpen() {
        return currentlyOpen;
    }

    public List<CategoryMenu> getMenu() {
        return menu;
    }


    public static class CategoryMenu {
        private String id;
        private String name;
        private List<FoodItem> foods;

        public String getId() { return id; }
        public String getName() { return name; }
        public List<FoodItem> getFoods() { return foods; }
    }

    public static class FoodItem {
        private String id;
        private String name;
        private String description;
        private double price;
        private String imageUrl;
        @SerializedName(value = "isAvailable", alternate = {"available"})
        private Boolean isAvailable = true;
        private String categoryId;
        private String categoryName;
        private List<String> tags;
        private String cuisine;
        private Integer spicyLevel;
        private Integer soldCount;

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public String getImageUrl() { return imageUrl; }
        public boolean isAvailable() { return isAvailable == null || isAvailable; }
        public String getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
        public List<String> getTags() { return tags; }
        public String getCuisine() { return cuisine; }
        public Integer getSpicyLevel() { return spicyLevel; }
        public Integer getSoldCount() { return soldCount != null ? soldCount : 0; }
    }
}
