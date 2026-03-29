package com.restaurant.backend.model;

import java.time.Instant;

public class Refund {
    private Long id;
    private Long orderId;
    private Long paymentId;
    private Long paymentSplitId; // null if refunding full payment
    private double amount;
    private String reason;
    private Long approvedBy; // manager/user ID who approved
    private Instant createdAt;
    private RefundStatus status = RefundStatus.PENDING;

    public enum RefundStatus {
        PENDING, APPROVED, REJECTED, PROCESSED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getPaymentSplitId() { return paymentSplitId; }
    public void setPaymentSplitId(Long paymentSplitId) { this.paymentSplitId = paymentSplitId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }
}


