package com.foodorderingapp.model.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class VendorDashboardResponse {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double completionRate;
    private BigDecimal averageOrderValue;
    private List<TrendData> orderTrends;
    private List<TopProductData> topSellingProducts;
    private Map<String, Long> orderStatusBreakdown;

    public VendorDashboardResponse() {}

    public VendorDashboardResponse(BigDecimal totalRevenue, Long totalOrders, Double completionRate,
                                   BigDecimal averageOrderValue, List<TrendData> orderTrends,
                                   List<TopProductData> topSellingProducts, Map<String, Long> orderStatusBreakdown) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.completionRate = completionRate;
        this.averageOrderValue = averageOrderValue;
        this.orderTrends = orderTrends;
        this.topSellingProducts = topSellingProducts;
        this.orderStatusBreakdown = orderStatusBreakdown;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public List<TrendData> getOrderTrends() {
        return orderTrends;
    }

    public void setOrderTrends(List<TrendData> orderTrends) {
        this.orderTrends = orderTrends;
    }

    public List<TopProductData> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopProductData> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public Map<String, Long> getOrderStatusBreakdown() {
        return orderStatusBreakdown;
    }

    public void setOrderStatusBreakdown(Map<String, Long> orderStatusBreakdown) {
        this.orderStatusBreakdown = orderStatusBreakdown;
    }
}
