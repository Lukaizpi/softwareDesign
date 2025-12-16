package com.restaurant.backend.model;

import java.util.ArrayList;
import java.util.List;

public class Role {
    private Long id;
    private String name;
    private String description;
    private Long merchantId; // null for SuperAdmin (cross-merchant)
    private List<Permission> permissions = new ArrayList<>();

    public Role() {}

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }

    public boolean hasPermission(String resource, String action) {
        String required = resource + ":" + action;
        return permissions.stream()
            .anyMatch(p -> p.getResource().equals(resource) && p.getAction().equals(action));
    }
}


