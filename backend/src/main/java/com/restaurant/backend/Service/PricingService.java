package com.restaurant.backend.Service;

import com.restaurant.backend.model.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PricingService {
    private final ProductService productService;
    private final TaxService taxService;
    private final DiscountService discountService;
    private final IngredientService ingredientService;

    public PricingService(ProductService productService, TaxService taxService, DiscountService discountService, IngredientService ingredientService) {
        this.productService = productService;
        this.taxService = taxService;
        this.discountService = discountService;
        this.ingredientService = ingredientService;
    }

    public OrderCalculationResult calculateOrderTotal(Order order, Long merchantId) {
        double subtotal = 0.0;
        double taxes = 0.0;
        double totalDiscount = 0.0;

        // Calculate subtotal from items (real-time, no stored prices)
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                // Get current product price (real-time)
                Product product = productService.getProductById(item.getProductId()).orElse(null);
                if (product == null) continue;

                // Calculate base price + ingredients cost
                double basePrice = product.getBasePrice();
                double ingredientCost = calculateIngredientCost(product);
                double itemPrice = basePrice + ingredientCost;

                // Apply item-level discounts
                double itemDiscount = applyItemDiscounts(product.getId(), itemPrice, merchantId);
                double discountedPrice = itemPrice - itemDiscount;
                totalDiscount += itemDiscount * item.getQuantity();

                double lineTotal = discountedPrice * item.getQuantity();
                subtotal += lineTotal;

                // Calculate tax (real-time rate)
                double taxRate = getTaxRate(product.getTaxId());
                taxes += lineTotal * taxRate;
            }
        }

        // Apply order-level discount
        double orderDiscount = order.getDiscountAmount();
        totalDiscount += orderDiscount;

        // Calculate final total
        double serviceCharge = Math.max(0, order.getServiceCharge());
        double tips = calculateTotalTips(order);
        double total = subtotal + taxes + serviceCharge + tips - totalDiscount;
        if (total < 0) total = 0.0;

        return new OrderCalculationResult(
            round2(subtotal),
            round2(taxes),
            round2(totalDiscount),
            round2(total)
        );
    }

    private double calculateIngredientCost(Product product) {
        if (product.getIngredientIds() == null || product.getIngredientIds().isEmpty()) {
            return 0.0;
        }
        // Calculate cost from ingredients (assuming 1 unit of each ingredient per product)
        java.util.Map<Long, Double> quantities = new java.util.HashMap<>();
        for (Long ingredientId : product.getIngredientIds()) {
            quantities.put(ingredientId, 1.0);
        }
        return ingredientService.calculateIngredientCost(product.getIngredientIds(), quantities);
    }

    private double applyItemDiscounts(Long productId, double price, Long merchantId) {
        // Get active discounts for this product
        List<Discount> discounts = discountService.getActiveDiscountsForProduct(productId, merchantId);
        double totalDiscount = 0.0;

        for (Discount discount : discounts) {
            if (discount.getValueType() == Discount.DiscountValueType.FIXED) {
                totalDiscount += discount.getValue();
            } else if (discount.getValueType() == Discount.DiscountValueType.PERCENTAGE) {
                totalDiscount += price * (discount.getValue() / 100.0);
            }
        }

        return Math.min(totalDiscount, price); // Don't discount more than price
    }

    private double getTaxRate(Long taxId) {
        if (taxId == null) return 0.0;
        return taxService.getById(taxId)
                .filter(Tax::isActive)
                .map(Tax::getRate)
                .orElse(0.0);
    }

    private double calculateTotalTips(Order order) {
        if (order.getPayments() != null) {
            return order.getPayments().stream()
                    .filter(p -> !p.isRefunded())
                    .mapToDouble(Payment::getTip)
                    .sum();
        }
        if (order.getPaymentSplits() != null) {
            return order.getPaymentSplits().stream()
                    .filter(s -> !s.isRefunded())
                    .mapToDouble(PaymentSplit::getTip)
                    .sum();
        }
        return 0.0;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static class OrderCalculationResult {
        public final double subtotal;
        public final double taxes;
        public final double totalDiscount;
        public final double total;

        public OrderCalculationResult(double subtotal, double taxes, double totalDiscount, double total) {
            this.subtotal = subtotal;
            this.taxes = taxes;
            this.totalDiscount = totalDiscount;
            this.total = total;
        }
    }
}
