package com.foodorderingapp.model;

import java.util.List;

public class ShopListResponse {
    private List<Shop> content;
    private int totalPages;
    private long totalElements;

    public List<Shop> getContent() {
        return content;
    }

    public List<Shop> getShops() {
        return content;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public static class Shop {
        private String id;
        private String name;
        private String description;
        private String address;
        private String openTime;
        private String closeTime;
        private String status;
        private boolean active;
        private String displayStatus;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAddress() {
            return address;
        }

        public String getOpenTime() {
            return openTime;
        }

        public String getCloseTime() {
            return closeTime;
        }

        public String getStatus() {
            return status;
        }

        public boolean isActive() {
            return active;
        }

        public String getDisplayStatus() {
            return displayStatus;
        }
    }
}
