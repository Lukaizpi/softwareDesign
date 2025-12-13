package com.restaurant.backend.model;

public class Product {

    private Long id;
    private String name;
    private String category; // ej: "food", "drink"
    private double price;
    private Long taxId;

    public Product() {
    }

    public Product(Long id, String name, String category, double price, Long taxId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.taxId = taxId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getTaxId() {
        return taxId;
    }

    public void setTaxId(Long taxId) {
        this.taxId = taxId;
    }
}