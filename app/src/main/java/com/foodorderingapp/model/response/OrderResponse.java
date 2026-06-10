package com.foodorderingapp.model.response;

import java.util.List;

public class OrderResponse {
    private String id;
    private String shopName;
    private String customerName;
    private String customerPhone;
    private double totalPrice;
    private String status;
    private String building;
    private String dropOff;
    private String cancelReason;
    private String createdAt;
    private List<OrderDetailResponse> details;

    public String getId() { return id; }
    public String getShopName() { return shopName; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getBuilding() { return building; }
    public String getDropOff() { return dropOff; }
    public String getCancelReason() { return cancelReason; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderDetailResponse> getDetails() { return details; }
}
