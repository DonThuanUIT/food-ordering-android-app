package com.foodorderingapp.model.request;

import java.util.List;

public class ReviewSubmitRequest {
    private Integer orderRating;     // Delivery/order rating (1-5)
    private String orderComment;     // Delivery/order comment
    
    private Integer shopRating;      // Shop rating (1-5)
    private String shopComment;      // Shop comment
    
    private List<FoodReviewItem> foodReviews; // Individual food item reviews

    public Integer getOrderRating() {
        return orderRating;
    }

    public void setOrderRating(Integer orderRating) {
        this.orderRating = orderRating;
    }

    public String getOrderComment() {
        return orderComment;
    }

    public void setOrderComment(String orderComment) {
        this.orderComment = orderComment;
    }

    public Integer getShopRating() {
        return shopRating;
    }

    public void setShopRating(Integer shopRating) {
        this.shopRating = shopRating;
    }

    public String getShopComment() {
        return shopComment;
    }

    public void setShopComment(String shopComment) {
        this.shopComment = shopComment;
    }

    public List<FoodReviewItem> getFoodReviews() {
        return foodReviews;
    }

    public void setFoodReviews(List<FoodReviewItem> foodReviews) {
        this.foodReviews = foodReviews;
    }

    public static class FoodReviewItem {
        private String foodId;
        private Integer rating;
        private String comment;

        public FoodReviewItem(String foodId, Integer rating, String comment) {
            this.foodId = foodId;
            this.rating = rating;
            this.comment = comment;
        }

        public String getFoodId() {
            return foodId;
        }

        public void setFoodId(String foodId) {
            this.foodId = foodId;
        }

        public Integer getRating() {
            return rating;
        }

        public void setRating(Integer rating) {
            this.rating = rating;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
