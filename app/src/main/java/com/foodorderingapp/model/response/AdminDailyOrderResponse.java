package com.foodorderingapp.model.response;

public class AdminDailyOrderResponse {
    private String date;
    private long totalOrders;

    public AdminDailyOrderResponse(String date, long totalOrders) {
        this.date = date;
        this.totalOrders = totalOrders;
    }

    public String getDate() {
        return date;
    }

    public long getTotalOrders() {
        return totalOrders;
    }
}
