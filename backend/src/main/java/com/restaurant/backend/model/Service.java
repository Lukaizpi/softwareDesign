package com.restaurant.backend.model;

public class Service {
    private Long id;
    private String name;
    private String description;
    private int durationMinutes; // service duration
    private double price;
    private Long merchantId;
    private Long branchId; // null if available at all branches
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}


