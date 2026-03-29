package com.restaurant.backend.model;

public class Permission {
    private Long id;
    private String resource; // e.g., "order", "payment", "product"
    private String action;    // e.g., "create", "update", "delete", "refund"
    private String description;

    public Permission() {}

    public Permission(String resource, String action) {
        this.resource = resource;
        this.action = action;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFullPermission() {
        return resource + ":" + action;
    }
}


