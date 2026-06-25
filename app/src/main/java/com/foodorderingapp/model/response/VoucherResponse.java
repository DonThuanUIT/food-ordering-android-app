package com.foodorderingapp.model.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class VoucherResponse {
    private UUID id;
    private UUID shopId;
    private String code;
    private String title;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private String applyType;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private List<UUID> foodIds;

    public VoucherResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getShopId() {
        return shopId;
    }

    public void setShopId(UUID shopId) {
        this.shopId = shopId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(BigDecimal minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public BigDecimal getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public void setMaxDiscountValue(BigDecimal maxDiscountValue) {
        this.maxDiscountValue = maxDiscountValue;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<UUID> getFoodIds() {
        return foodIds;
    }

    public void setFoodIds(List<UUID> foodIds) {
        this.foodIds = foodIds;
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

