package com.restaurant.backend.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private Long id;
    private String name;
    private String category; // legacy support
    private ProductType type; // FOOD, DRINK, SERVICE
    private double basePrice; // base price before ingredients
    private Long taxId;
    private Long merchantId;
    private boolean available = true;
    private List<Long> ingredientIds = new ArrayList<>(); // ingredients used in this product
    private List<Ingredient> ingredients = new ArrayList<>();

    public Product() {
    }

    public Product(Long id, String name, String category, double price, Long taxId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.basePrice = price;
        this.taxId = taxId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ProductType getType() { return type; }
    public void setType(ProductType type) { this.type = type; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }

    @Deprecated
    public double getPrice() { return basePrice; }
    @Deprecated
    public void setPrice(double price) { this.basePrice = price; }

    public Long getTaxId() { return taxId; }
    public void setTaxId(Long taxId) { this.taxId = taxId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<Long> getIngredientIds() { return ingredientIds; }
    public void setIngredientIds(List<Long> ingredientIds) { this.ingredientIds = ingredientIds; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }
}