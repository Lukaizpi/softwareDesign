package com.restaurant.backend.model;

public class OrderItem {
    private Long id; // Unique ID for this item instance in the order
    private Long productId;
    private String productName;
    private double unitPrice;
    private double taxRate;
    private int quantity;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getTaxRate() { return taxRate; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}