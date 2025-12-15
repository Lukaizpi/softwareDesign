package com.restaurant.backend.model;

import java.time.Instant;
import java.time.LocalDateTime;

public class Discount {
    private Long id;
    private String code; // optional discount code
    private String name;
    private DiscountType type; // PRODUCT or ORDER
    private DiscountValueType valueType; // FIXED or PERCENTAGE
    private double value; // amount or percentage
    private Long productId; // null for order-level discounts
    private Long merchantId;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer maxUsage; // null for unlimited
    private Integer currentUsage = 0;
    private DiscountStatus status = DiscountStatus.ACTIVE;
    private Instant createdAt;

    public enum DiscountType {
        PRODUCT, ORDER
    }

    public enum DiscountValueType {
        FIXED, PERCENTAGE
    }

    public enum DiscountStatus {
        ACTIVE, SCHEDULED, INACTIVE
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public DiscountType getType() { return type; }
    public void setType(DiscountType type) { this.type = type; }

    public DiscountValueType getValueType() { return valueType; }
    public void setValueType(DiscountValueType valueType) { this.valueType = valueType; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }

    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }

    public Integer getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(Integer currentUsage) { this.currentUsage = currentUsage; }

    public DiscountStatus getStatus() { return status; }
    public void setStatus(DiscountStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        if (status != DiscountStatus.ACTIVE) return false;
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validTo != null && now.isAfter(validTo)) return false;
        if (maxUsage != null && currentUsage >= maxUsage) return false;
        return true;
    }
}

