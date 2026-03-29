package com.restaurant.backend.Service;

import com.restaurant.backend.model.Discount;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class DiscountService {
    private final Map<Long, Discount> discounts = new HashMap<>();
    private final AtomicLong discountSeq = new AtomicLong(1);

    public DiscountService() {
        // Initialize with some sample discounts
        initializeSampleDiscounts();
    }

    private void initializeSampleDiscounts() {
        // Sample order-level discount
        Discount orderDiscount = new Discount();
        orderDiscount.setId(discountSeq.getAndIncrement());
        orderDiscount.setName("10% Off Orders");
        orderDiscount.setType(Discount.DiscountType.ORDER);
        orderDiscount.setValueType(Discount.DiscountValueType.PERCENTAGE);
        orderDiscount.setValue(10.0);
        orderDiscount.setStatus(Discount.DiscountStatus.ACTIVE);
        orderDiscount.setValidFrom(LocalDateTime.now().minusDays(1));
        orderDiscount.setValidTo(LocalDateTime.now().plusDays(30));
        orderDiscount.setMaxUsage(100);
        orderDiscount.setCurrentUsage(0);
        discounts.put(orderDiscount.getId(), orderDiscount);
    }

    public List<Discount> getAllDiscounts() {
        return new ArrayList<>(discounts.values());
    }

    public List<Discount> getActiveDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        return discounts.values().stream()
                .filter(d -> d.getStatus() == Discount.DiscountStatus.ACTIVE)
                .filter(d -> d.getValidFrom() == null || !now.isBefore(d.getValidFrom()))
                .filter(d -> d.getValidTo() == null || !now.isAfter(d.getValidTo()))
                .filter(d -> d.getMaxUsage() == null || d.getCurrentUsage() < d.getMaxUsage())
                .collect(Collectors.toList());
    }

    public List<Discount> getActiveDiscountsForProduct(Long productId, Long merchantId) {
        return getActiveDiscounts().stream()
                .filter(d -> d.getType() == Discount.DiscountType.PRODUCT)
                .filter(d -> d.getProductId() != null && d.getProductId().equals(productId))
                .filter(d -> d.getMerchantId() == null || d.getMerchantId().equals(merchantId))
                .collect(Collectors.toList());
    }

    public List<Discount> getActiveOrderLevelDiscounts(Long merchantId) {
        return getActiveDiscounts().stream()
                .filter(d -> d.getType() == Discount.DiscountType.ORDER)
                .filter(d -> d.getMerchantId() == null || d.getMerchantId().equals(merchantId))
                .collect(Collectors.toList());
    }

    public Optional<Discount> getDiscountById(Long id) {
        return Optional.ofNullable(discounts.get(id));
    }

    public Discount createDiscount(String name, Discount.DiscountType type, Discount.DiscountValueType valueType,
                                   double value, Long productId, Long merchantId, LocalDateTime validFrom,
                                   LocalDateTime validTo, Integer maxUsage) {
        Discount discount = new Discount();
        discount.setId(discountSeq.getAndIncrement());
        discount.setName(name);
        discount.setType(type);
        discount.setValueType(valueType);
        discount.setValue(value);
        discount.setProductId(productId);
        discount.setMerchantId(merchantId);
        discount.setValidFrom(validFrom);
        discount.setValidTo(validTo);
        discount.setMaxUsage(maxUsage);
        discount.setCurrentUsage(0);
        discount.setStatus(Discount.DiscountStatus.ACTIVE);
        discount.setCreatedAt(java.time.Instant.now());

        discounts.put(discount.getId(), discount);
        return discount;
    }

    public Optional<Discount> updateDiscount(Long id, String name, Discount.DiscountStatus status,
                                             LocalDateTime validFrom, LocalDateTime validTo, Integer maxUsage) {
        Discount discount = discounts.get(id);
        if (discount == null) return Optional.empty();

        if (name != null) discount.setName(name);
        if (status != null) discount.setStatus(status);
        if (validFrom != null) discount.setValidFrom(validFrom);
        if (validTo != null) discount.setValidTo(validTo);
        if (maxUsage != null) discount.setMaxUsage(maxUsage);

        return Optional.of(discount);
    }

    public void incrementUsage(Long discountId) {
        Discount discount = discounts.get(discountId);
        if (discount != null && discount.getCurrentUsage() != null) {
            discount.setCurrentUsage(discount.getCurrentUsage() + 1);
        }
    }

    public boolean isValid(Discount discount) {
        if (discount == null) return false;
        if (discount.getStatus() != Discount.DiscountStatus.ACTIVE) return false;

        LocalDateTime now = LocalDateTime.now();
        if (discount.getValidFrom() != null && now.isBefore(discount.getValidFrom())) return false;
        if (discount.getValidTo() != null && now.isAfter(discount.getValidTo())) return false;
        if (discount.getMaxUsage() != null && discount.getCurrentUsage() >= discount.getMaxUsage()) return false;

        return true;
    }
}
