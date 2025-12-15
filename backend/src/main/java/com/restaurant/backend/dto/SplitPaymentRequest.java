package com.restaurant.backend.dto;

import com.restaurant.backend.model.PaymentMethod;
import java.util.List;

public class SplitPaymentRequest {
    public String customerName;
    public PaymentMethod method;
    public double amount;
    public double tip;
    public List<Long> itemIds;
}
