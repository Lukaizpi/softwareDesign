package com.restaurant.backend.model;

public class Ingredient {
    private Long id;
    private String name;
    private String unit; // "kg", "liter", "piece", etc.
    private double price; // cost per unit
    private Long categoryId;
    private Long merchantId;
    private boolean available = true;
    private Double stock; // null for unlimited

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public Double getStock() { return stock; }
    public void setStock(Double stock) { this.stock = stock; }
}


