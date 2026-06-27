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
    private String cancelReason;
    private String createdAt;
    private List<OrderDetailResponse> details;
    private String voucherCode;
    private double discountAmount;
    private boolean isReviewed;

    private String shipperId;
    private String shipperName;
    private String shipperPhone;
    private Double shipperLatitude;
    private Double shipperLongitude;

    private String shopId;
    private String shopAddress;
    private Double shopLatitude;
    private Double shopLongitude;
    private Double buildingLatitude;
    private Double buildingLongitude;

    public String getId() { return id; }
    public String getShopName() { return shopName; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getBuilding() { return building; }
    public String getCancelReason() { return cancelReason; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderDetailResponse> getDetails() { return details; }
    public String getVoucherCode() { return voucherCode; }
    public double getDiscountAmount() { return discountAmount; }
    public boolean isReviewed() { return isReviewed; }
    public void setReviewed(boolean reviewed) { isReviewed = reviewed; }

    public String getShipperId() { return shipperId; }
    public String getShipperName() { return shipperName; }
    public String getShipperPhone() { return shipperPhone; }
    public Double getShipperLatitude() { return shipperLatitude; }
    public Double getShipperLongitude() { return shipperLongitude; }

    public String getShopId() { return shopId; }
    public String getShopAddress() { return shopAddress; }
    public Double getShopLatitude() { return shopLatitude; }
    public Double getShopLongitude() { return shopLongitude; }
    public Double getBuildingLatitude() { return buildingLatitude; }
    public Double getBuildingLongitude() { return buildingLongitude; }
}
