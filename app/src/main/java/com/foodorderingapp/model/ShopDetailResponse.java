package com.foodorderingapp.model;

import java.math.BigDecimal;
import java.util.List;

public class ShopDetailResponse {
    private String id;
    private String name;
    private String address;
    private String description;
    private List<Category> menu;

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

    public List<Category> getMenu() {
        return menu;
    }

    public List<Category> getCategories() {
        return menu;
    }

    public static class Category {
        private String id;
        private String name;
        private List<Food> foods;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<Food> getFoods() {
            return foods;
        }
    }

    public static class Food {
        private String id;
        private String name;
        private String description;
        private BigDecimal price;
        private String imageUrl;
        private Boolean isAvailable;
        private String categoryId;
        private String categoryName;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public Boolean getAvailable() {
            return isAvailable;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }
    }
}
