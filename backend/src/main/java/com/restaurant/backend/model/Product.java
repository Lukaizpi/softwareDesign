package com.restaurant.backend.model;

public class Product {

    private Long id;
    private String name;
    private String category; // ej: "food", "drink"
    private double price;
    private double taxRate;  // porcentaje, ej: 0.10 = 10%

    public Product() {
    }

    public Product(Long id, String name, String category, double price, double taxRate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.taxRate = taxRate;
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

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }
}