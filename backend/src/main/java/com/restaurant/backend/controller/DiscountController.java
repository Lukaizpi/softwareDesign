package com.restaurant.backend.controller;

import com.restaurant.backend.Service.DiscountService;
import com.restaurant.backend.model.Discount;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discounts")
@CrossOrigin(origins = "*")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @GetMapping
    public List<Discount> getAllDiscounts() {
        return discountService.getAllDiscounts();
    }

    @GetMapping("/active")
    public List<Discount> getActiveDiscounts() {
        return discountService.getActiveDiscounts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Discount> getDiscountById(@PathVariable Long id) {
        return discountService.getDiscountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createDiscount(@RequestBody CreateDiscountRequest req) {
        try {
            Discount discount = discountService.createDiscount(
                    req.name,
                    Discount.DiscountType.valueOf(req.type),
                    Discount.DiscountValueType.valueOf(req.valueType),
                    req.value,
                    req.productId,
                    req.merchantId,
                    req.validFrom != null ? LocalDateTime.parse(req.validFrom) : null,
                    req.validTo != null ? LocalDateTime.parse(req.validTo) : null,
                    req.maxUsage
            );
            return ResponseEntity.ok(discount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid discount type or value type"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Discount> updateDiscount(@PathVariable Long id,
                                                    @RequestBody UpdateDiscountRequest req) {
        return discountService.updateDiscount(
                id,
                req.name,
                req.status != null ? Discount.DiscountStatus.valueOf(req.status) : null,
                req.validFrom != null ? LocalDateTime.parse(req.validFrom) : null,
                req.validTo != null ? LocalDateTime.parse(req.validTo) : null,
                req.maxUsage
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner classes for DTOs
    public static class CreateDiscountRequest {
        public String name;
        public String type; // "PRODUCT" or "ORDER"
        public String valueType; // "FIXED" or "PERCENTAGE"
        public double value;
        public Long productId;
        public Long merchantId;
        public String validFrom;
        public String validTo;
        public Integer maxUsage;
    }

    public static class UpdateDiscountRequest {
        public String name;
        public String status; // "ACTIVE", "SCHEDULED", "INACTIVE"
        public String validFrom;
        public String validTo;
        public Integer maxUsage;
    }
}
