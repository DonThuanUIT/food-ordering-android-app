package com.foodorderingapp.model.response;

public class StudentReviewResponse {
    private String id;
    private Integer rating;
    private String comment;
    private String createdAt;
    private String orderId;
    private String shopName;
    private Double totalPrice;

    public String getId() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getShopName() {
        return shopName;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }
}
