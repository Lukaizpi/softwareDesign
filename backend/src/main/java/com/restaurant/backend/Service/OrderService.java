package com.restaurant.backend.Service;

import com.restaurant.backend.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final AtomicLong orderSeq = new AtomicLong(1);
    private final AtomicLong paymentSeq = new AtomicLong(1);
    private final AtomicLong paymentSplitSeq = new AtomicLong(1);
    private final AtomicLong orderItemSeq = new AtomicLong(1);
    private final ProductService productService;
    private final TaxService taxService;
    private final PricingService pricingService;

    public OrderService(ProductService productService, TaxService taxService, PricingService pricingService) {
        this.productService = productService;
        this.taxService = taxService;
        this.pricingService = pricingService;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> getOrderById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Order createOrder(String tableNumber, String employeeName) {
        Order o = new Order();
        o.setId(orderSeq.getAndIncrement());
        o.setTableNumber(tableNumber);
        o.setEmployeeName(employeeName);
        o.setCreatedAt(Instant.now());
        o.setStatus(OrderStatus.OPEN);
        o.setItems(new ArrayList<>());

        recalcTotals(o);
        orders.put(o.getId(), o);
        return o;
    }

    public Optional<Order> addItemToOrder(Long orderId, Long productId, int quantity) {
        if (quantity <= 0)
            return Optional.empty();

        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();

        Product product = productService.getProductById(productId).orElse(null);
        if (product == null)
            return Optional.empty();

        double taxRate = 0.0;
        if (product.getTaxId() != null) {
            taxRate = taxService.getById(product.getTaxId())
                    .filter(Tax::isActive)
                    .map(Tax::getRate)
                    .orElse(0.0);
        }

        OrderItem item = new OrderItem();
        item.setId(orderItemSeq.getAndIncrement());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setUnitPrice(product.getBasePrice());
        item.setQuantity(quantity);
        item.setTaxRate(taxRate); // snapshot

        order.getItems().add(item);
        recalcTotals(order);

        return Optional.of(order);
    }

    public Optional<Order> cancelOrder(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();

        order.setStatus(OrderStatus.CANCELLED);
        return Optional.of(order);
    }

    public Optional<Order> markAsPaid(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();

        order.setStatus(OrderStatus.PAID);
        return Optional.of(order);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Optional<Order> updateStatus(Long orderId, OrderStatus status) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();

        // regla simple: no tocar cerradas (PAID/CANCELLED)
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CANCELLED) {
            return Optional.empty();
        }

        order.setStatus(status);
        return Optional.of(order);
    }

    private void recalcTotals(Order order) {
        // Use PricingService for real-time calculation if available
        try {
            PricingService.OrderCalculationResult result = pricingService.calculateOrderTotal(order, order.getMerchantId());
            order.setSubtotal(result.subtotal);
            order.setTaxes(result.taxes);
            order.setDiscountAmount(result.totalDiscount);
            order.setTotal(result.total);
        } catch (Exception e) {
            // Fallback to simple calculation if PricingService fails
            double subtotal = 0;
            double taxes = 0;

            for (OrderItem it : order.getItems()) {
                double line = it.getUnitPrice() * it.getQuantity();
                subtotal += line;
                taxes += line * it.getTaxRate();
            }

            double discount = Math.max(0, order.getDiscountAmount());
            double service = Math.max(0, order.getServiceCharge());

            double tip = order.getPayments().stream()
                    .filter(p -> !p.isRefunded())
                    .mapToDouble(Payment::getTip)
                    .sum();

            double total = subtotal + taxes + service + tip - discount;
            if (total < 0) total = 0;

            order.setSubtotal(round2(subtotal));
            order.setTaxes(round2(taxes));
            order.setTotal(round2(total));
        }
    }

    private double paidSoFar(Order order) {
        return order.getPayments().stream()
                .filter(p -> !p.isRefunded())
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    private boolean isFullyPaid(Order order) {
        return round2(paidSoFar(order)) >= round2(order.getTotal());
    }

    public Optional<Order> applyDiscount(Long orderId, double amount) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();

        order.setDiscountAmount(round2(Math.max(0, amount)));
        recalcTotals(order);
        if (isFullyPaid(order))
            order.setStatus(OrderStatus.PAID);

        return Optional.of(order);
    }

    public Optional<Order> addPayment(Long orderId, com.restaurant.backend.model.PaymentMethod method, double amount,
            double tip) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();

        if (amount <= 0)
            return Optional.empty();
        if (tip < 0)
            return Optional.empty();

        Payment p = new Payment();
        p.setId(paymentSeq.getAndIncrement());
        p.setMethod(method);
        p.setAmount(round2(amount));
        p.setTip(round2(tip));
        p.setCreatedAt(Instant.now());
        p.setRefunded(false);

        order.getPayments().add(p);

        recalcTotals(order);
        if (isFullyPaid(order)) {
            order.setStatus(OrderStatus.PAID);
        }
        return Optional.of(order);
    }

    public Optional<Order> refund(Long orderId, double amount) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.PAID)
            return Optional.empty();

        double target = amount <= 0 ? paidSoFar(order) : amount;
        target = round2(Math.max(0, target));

        double remaining = target;

        for (Payment p : order.getPayments()) {
            if (p.isRefunded())
                continue;
            if (remaining <= 0)
                break;

            double refundable = p.getAmount();
            p.setRefunded(true);
            remaining = round2(remaining - refundable);
        }

        order.setRefunded(true);
        return Optional.of(order);
    }

    // Split Payment Methods
    public Optional<Order> startSplitPayment(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();
        if (order.getItems() == null || order.getItems().isEmpty())
            return Optional.empty();

        order.setItemsLocked(true);
        if (order.getPaymentSplits() == null) {
            order.setPaymentSplits(new ArrayList<>());
        }
        return Optional.of(order);
    }

    public Optional<Order> addPaymentSplit(Long orderId, String customerName, PaymentMethod method,
                                           double amount, double tip, List<Long> itemIds) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (!order.isItemsLocked())
            return Optional.empty();
        if (order.getStatus() != OrderStatus.OPEN)
            return Optional.empty();
        if (tip < 0)
            return Optional.empty();
        if (itemIds == null || itemIds.isEmpty())
            return Optional.empty();

        // Verify items exist and are not already assigned
        Set<Long> assignedItemIds = new HashSet<>();
        if (order.getPaymentSplits() != null) {
            for (PaymentSplit split : order.getPaymentSplits()) {
                if (split.getOrderItemIds() != null) {
                    assignedItemIds.addAll(split.getOrderItemIds());
                }
            }
        }

        // Calculate amount automatically based on selected items
        double calculatedAmount = 0.0;
        for (Long itemId : itemIds) {
            if (assignedItemIds.contains(itemId)) {
                return Optional.empty(); // Item already assigned
            }
            OrderItem item = order.getItems().stream()
                    .filter(i -> i.getId() != null && i.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
            if (item == null) {
                return Optional.empty(); // Item doesn't exist
            }
            // Calculate item total (price * quantity + tax)
            double lineTotal = item.getUnitPrice() * item.getQuantity();
            double taxAmount = lineTotal * item.getTaxRate();
            calculatedAmount += lineTotal + taxAmount;
        }

        // Use calculated amount (ignore provided amount parameter)
        double finalAmount = round2(calculatedAmount);
        if (finalAmount <= 0) {
            return Optional.empty();
        }

        PaymentSplit split = new PaymentSplit();
        split.setId(paymentSplitSeq.getAndIncrement());
        split.setOrderId(orderId);
        split.setCustomerName(customerName);
        split.setMethod(method);
        split.setAmount(finalAmount);
        split.setTip(round2(tip));
        split.setOrderItemIds(new ArrayList<>(itemIds));
        split.setCreatedAt(Instant.now());
        split.setRefunded(false);

        if (order.getPaymentSplits() == null) {
            order.setPaymentSplits(new ArrayList<>());
        }
        order.getPaymentSplits().add(split);

        // Recalculate totals including splits
        recalcTotalsWithSplits(order);

        return Optional.of(order);
    }

    public Optional<Order> completeSplitPayment(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null)
            return Optional.empty();
        if (!order.isItemsLocked())
            return Optional.empty();
        if (order.getPaymentSplits() == null || order.getPaymentSplits().isEmpty())
            return Optional.empty();

        // Check if all items are assigned
        Set<Long> allItemIds = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            if (item.getId() != null) {
                allItemIds.add(item.getId());
            }
        }

        Set<Long> assignedItemIds = new HashSet<>();
        for (PaymentSplit split : order.getPaymentSplits()) {
            if (split.getOrderItemIds() != null) {
                assignedItemIds.addAll(split.getOrderItemIds());
            }
        }

        if (!assignedItemIds.equals(allItemIds)) {
            return Optional.empty(); // Not all items assigned
        }

        // Convert splits to regular payments
        for (PaymentSplit split : order.getPaymentSplits()) {
            Payment payment = new Payment();
            payment.setId(paymentSeq.getAndIncrement());
            payment.setMethod(split.getMethod());
            payment.setAmount(split.getAmount());
            payment.setTip(split.getTip());
            payment.setCreatedAt(split.getCreatedAt());
            payment.setRefunded(false);
            order.getPayments().add(payment);
        }

        order.setItemsLocked(false);
        order.setStatus(OrderStatus.PAID);
        recalcTotals(order);

        return Optional.of(order);
    }

    private void recalcTotalsWithSplits(Order order) {
        // Calculate totals based on assigned items in splits
        double subtotal = 0;
        double taxes = 0;
        double totalSplitTips = 0;

        Set<Long> assignedItemIds = new HashSet<>();
        if (order.getPaymentSplits() != null) {
            for (PaymentSplit split : order.getPaymentSplits()) {
                if (split.getOrderItemIds() != null) {
                    assignedItemIds.addAll(split.getOrderItemIds());
                    totalSplitTips += split.getTip();
                }
            }
        }

        // Calculate subtotal and taxes for assigned items
        for (OrderItem item : order.getItems()) {
            if (item.getId() != null && assignedItemIds.contains(item.getId())) {
                double line = item.getUnitPrice() * item.getQuantity();
                subtotal += line;
                taxes += line * item.getTaxRate();
            }
        }

        double discount = Math.max(0, order.getDiscountAmount());
        double service = Math.max(0, order.getServiceCharge());
        double total = subtotal + taxes + service + totalSplitTips - discount;
        if (total < 0) total = 0;

        order.setSubtotal(round2(subtotal));
        order.setTaxes(round2(taxes));
        order.setTotal(round2(total));
    }
}