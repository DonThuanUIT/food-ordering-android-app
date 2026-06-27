package com.foodorderingapp.utils;

import com.foodorderingapp.model.response.ShopResponse;
import com.foodorderingapp.model.response.FoodResponse;
import com.foodorderingapp.model.response.CategoryResponse;
import com.foodorderingapp.model.response.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class VendorDataCache {
    private static ShopResponse shopResponse;
    private static List<FoodResponse> foodList;
    private static List<CategoryResponse> categories;
    private static List<OrderResponse> orderList;

    public static ShopResponse getShopResponse() {
        return shopResponse;
    }

    public static void setShopResponse(ShopResponse shop) {
        shopResponse = shop;
    }

    public static List<FoodResponse> getFoodList() {
        return foodList;
    }

    public static void setFoodList(List<FoodResponse> foods) {
        if (foods != null) {
            foodList = new ArrayList<>(foods);
        } else {
            foodList = null;
        }
    }

    public static List<CategoryResponse> getCategories() {
        return categories;
    }

    public static void setCategories(List<CategoryResponse> cats) {
        if (cats != null) {
            categories = new ArrayList<>(cats);
        } else {
            categories = null;
        }
    }

    public static List<OrderResponse> getOrderList() {
        return orderList;
    }

    public static void setOrderList(List<OrderResponse> orders) {
        if (orders != null) {
            orderList = new ArrayList<>(orders);
        } else {
            orderList = null;
        }
    }

    public static void clear() {
        shopResponse = null;
        foodList = null;
        categories = null;
        orderList = null;
    }
}
