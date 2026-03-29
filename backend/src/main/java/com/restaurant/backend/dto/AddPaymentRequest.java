package com.restaurant.backend.dto;

import com.restaurant.backend.model.PaymentMethod;

public class AddPaymentRequest {
    public PaymentMethod method;
    public double amount; // principal
    public double tip;    // opcional
}
