package com.foodorderingapp.model.response;

import java.util.List;

public class ShopCartResponse {
    private String shopId;
    private String shopName;
    private List<CartItemResponse> items;

    public String getShopId() { return shopId; }
    public String getShopName() { return shopName; }
    public List<CartItemResponse> getItems() { return items; }
}
