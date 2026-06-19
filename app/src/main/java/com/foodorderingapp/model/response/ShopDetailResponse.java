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
    private Boolean isOpen;
    private List<CategoryMenu> menu;

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

    public Boolean getIsOpen() {
        return isOpen;
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

        public String getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public double getPrice() { return price; }
        public String getImageUrl() { return imageUrl; }
        public boolean isAvailable() { return isAvailable == null || isAvailable; }
        public String getCategoryId() { return categoryId; }
        public String getCategoryName() { return categoryName; }
    }
}
