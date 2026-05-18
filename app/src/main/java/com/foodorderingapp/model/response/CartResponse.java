package com.foodorderingapp.model.response;

import java.util.List;

public class CartResponse {
    private List<ShopCartResponse> shops;
    private double totalAmount;

    public List<ShopCartResponse> getShops() { return shops; }
    public double getTotalAmount() { return totalAmount; }
}
