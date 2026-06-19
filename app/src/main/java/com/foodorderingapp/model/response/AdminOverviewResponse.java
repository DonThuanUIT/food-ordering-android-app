package com.foodorderingapp.model.response;

import java.util.List;

public class AdminOverviewResponse {
    private long totalUsers;
    private long totalShops;
    private long pendingShops;
    private long approvedShops;
    private List<AdminDailyOrderResponse> dailyOrders;

    public AdminOverviewResponse(long totalUsers,
                                 long totalShops,
                                 long pendingShops,
                                 long approvedShops,
                                 List<AdminDailyOrderResponse> dailyOrders) {
        this.totalUsers = totalUsers;
        this.totalShops = totalShops;
        this.pendingShops = pendingShops;
        this.approvedShops = approvedShops;
        this.dailyOrders = dailyOrders;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalShops() {
        return totalShops;
    }

    public long getPendingShops() {
        return pendingShops;
    }

    public long getApprovedShops() {
        return approvedShops;
    }

    public List<AdminDailyOrderResponse> getDailyOrders() {
        return dailyOrders;
    }
}
