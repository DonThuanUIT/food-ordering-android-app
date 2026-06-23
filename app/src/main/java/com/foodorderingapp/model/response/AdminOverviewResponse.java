package com.foodorderingapp.model.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminOverviewResponse {
    private long totalUsers;
    private long totalShops;
    private long pendingShops;
    private long approvedShops;
    private long rejectedShops;
    private long bannedShops;
    private BigDecimal totalSystemRevenue;
    private long totalSystemOrders;
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

    public long getRejectedShops() {
        return rejectedShops;
    }

    public long getBannedShops() {
        return bannedShops;
    }

    public BigDecimal getTotalSystemRevenue() {
        return totalSystemRevenue == null ? BigDecimal.ZERO : totalSystemRevenue;
    }

    public long getTotalSystemOrders() {
        return totalSystemOrders;
    }

    public List<AdminDailyOrderResponse> getDailyOrders() {
        if (dailyOrders != null && !dailyOrders.isEmpty()) {
            return dailyOrders;
        }

        List<AdminDailyOrderResponse> statusBreakdown = new ArrayList<>();
        statusBreakdown.add(new AdminDailyOrderResponse("Pending", pendingShops));
        statusBreakdown.add(new AdminDailyOrderResponse("Approved", approvedShops));
        statusBreakdown.add(new AdminDailyOrderResponse("Rejected", rejectedShops));
        statusBreakdown.add(new AdminDailyOrderResponse("Banned", bannedShops));
        return statusBreakdown;
    }
}
