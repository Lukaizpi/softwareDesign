package com.restaurant.backend.model;

public class Tax {
    private Long id;
    private String code;        
    private String name;
    private double rate;        
    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}