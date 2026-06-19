package com.foodorderingapp.model.response;

import java.util.List;

public class VoucherResponse {
    private String id;
    private String shopId;
    private String code;
    private String title;
    private String discountType;
    private double discountValue;
    private Double minOrderValue;
    private Double maxDiscountValue;
    private String applyType;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private List<String> foodIds;

    public String getId() {
        return id;
    }

    public String getShopId() {
        return shopId;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDiscountType() {
        return discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public Double getMinOrderValue() {
        return minOrderValue;
    }

    public Double getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public String getApplyType() {
        return applyType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public List<String> getFoodIds() {
        return foodIds;
    }

    public String getDisplayText() {
        String safeCode = code == null ? "" : code;
        String safeTitle = title == null || title.trim().isEmpty() ? "Voucher" : title;
        return safeCode + " - " + safeTitle;
    }

    @Override
    public String toString() {
        return getDisplayText();
    }
}
