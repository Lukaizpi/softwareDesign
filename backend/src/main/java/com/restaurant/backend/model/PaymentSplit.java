package com.restaurant.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PaymentSplit {
    private Long id;
    private Long paymentId;
    private Long orderId;
    private String customerName; // optional, for split payments
    private PaymentMethod method;
    private double amount;
    private double tip;
    private Instant createdAt;
    private boolean refunded = false;
    private List<Long> orderItemIds = new ArrayList<>(); // items assigned to this split

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getTip() { return tip; }
    public void setTip(double tip) { this.tip = tip; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isRefunded() { return refunded; }
    public void setRefunded(boolean refunded) { this.refunded = refunded; }

    public List<Long> getOrderItemIds() { return orderItemIds; }
    public void setOrderItemIds(List<Long> orderItemIds) { this.orderItemIds = orderItemIds; }
}


