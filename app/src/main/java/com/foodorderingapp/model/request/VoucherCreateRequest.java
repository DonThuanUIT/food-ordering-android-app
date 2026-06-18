package com.foodorderingapp.model.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class VoucherCreateRequest {
    private String code;
    private String title;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private String applyType;
    private String startDate;
    private String endDate;
    private List<UUID> foodIds;

    public VoucherCreateRequest() {
    }

    public VoucherCreateRequest(String code, String title, String discountType, BigDecimal discountValue,
                                BigDecimal minOrderValue, BigDecimal maxDiscountValue, String applyType,
                                String startDate, String endDate, List<UUID> foodIds) {
        this.code = code;
        this.title = title;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.maxDiscountValue = maxDiscountValue;
        this.applyType = applyType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.foodIds = foodIds;
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

    public List<UUID> getFoodIds() {
        return foodIds;
    }

    public void setFoodIds(List<UUID> foodIds) {
        this.foodIds = foodIds;
    }
}
