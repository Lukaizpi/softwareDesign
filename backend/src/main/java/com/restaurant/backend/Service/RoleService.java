package com.restaurant.backend.Service;

import com.restaurant.backend.model.Permission;
import com.restaurant.backend.model.Role;
import com.restaurant.backend.model.User;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RoleService {
    private final Map<Long, Role> roles = new HashMap<>();
    private final Map<Long, Permission> permissions = new HashMap<>();
    private final AtomicLong roleSeq = new AtomicLong(1);
    private final AtomicLong permissionSeq = new AtomicLong(1);

    public RoleService() {
        initializePermissions();
        initializeRoles();
    }

    private void initializePermissions() {
        // Order permissions
        createPermission("order", "create");
        createPermission("order", "read");
        createPermission("order", "update");
        createPermission("order", "delete");
        createPermission("order", "cancel");
        createPermission("order", "refund");

        // Payment permissions
        createPermission("payment", "create");
        createPermission("payment", "refund");

        // Product permissions
        createPermission("product", "create");
        createPermission("product", "update");
        createPermission("product", "delete");
        createPermission("ingredient", "create");
        createPermission("ingredient", "update");

        // Discount permissions
        createPermission("discount", "create");
        createPermission("discount", "update");
        createPermission("discount", "delete");

        // Tax permissions
        createPermission("taxRate", "create");
        createPermission("taxRate", "update");
        createPermission("taxRate", "delete");

        // Reservation permissions
        createPermission("reservation", "create");
        createPermission("reservation", "update");
        createPermission("reservation", "cancel");

        // User management
        createPermission("user", "read");
        createPermission("user", "create");
        createPermission("user", "update");
        createPermission("user", "delete");
        createPermission("role", "manage");
    }

    private Permission createPermission(String resource, String action) {
        Permission p = new Permission(resource, action);
        p.setId(permissionSeq.getAndIncrement());
        p.setDescription(resource + " " + action);
        permissions.put(p.getId(), p);
        return p;
    }

    private void initializeRoles() {
        // SuperAdmin - all permissions
        Role superAdmin = new Role("SuperAdmin", "Cross-merchant administrator");
        superAdmin.setId(roleSeq.getAndIncrement());
        superAdmin.setMerchantId(null); // cross-merchant
        superAdmin.setPermissions(new ArrayList<>(permissions.values()));
        roles.put(superAdmin.getId(), superAdmin);

        // Manager - most permissions except user management
        Role manager = new Role("Manager", "Merchant manager");
        manager.setId(roleSeq.getAndIncrement());
        manager.setPermissions(getPermissionsForRole("Manager"));
        roles.put(manager.getId(), manager);

        // Employee - limited permissions
        Role employee = new Role("Employee", "Regular employee");
        employee.setId(roleSeq.getAndIncrement());
        employee.setPermissions(getPermissionsForRole("Employee"));
        roles.put(employee.getId(), employee);
    }

    private List<Permission> getPermissionsForRole(String roleName) {
        List<Permission> perms = new ArrayList<>();
        if ("Manager".equals(roleName)) {
            perms.addAll(getPermissionsByResource("order"));
            perms.addAll(getPermissionsByResource("payment"));
            perms.addAll(getPermissionsByResource("product"));
            perms.addAll(getPermissionsByResource("ingredient"));
            perms.addAll(getPermissionsByResource("discount"));
            perms.addAll(getPermissionsByResource("taxRate"));
            perms.addAll(getPermissionsByResource("reservation"));
            // Manager can only read users, not create/update/delete them
            perms.add(getPermission("user", "read"));
        } else if ("Employee".equals(roleName)) {
            perms.add(getPermission("order", "create"));
            perms.add(getPermission("order", "read"));
            perms.add(getPermission("order", "update"));
            perms.add(getPermission("payment", "create"));
            perms.add(getPermission("reservation", "create"));
            perms.add(getPermission("reservation", "read"));
        }
        return perms;
    }

    private List<Permission> getPermissionsByResource(String resource) {
        List<Permission> result = new ArrayList<>();
        for (Permission p : permissions.values()) {
            if (p.getResource().equals(resource)) {
                result.add(p);
            }
        }
        return result;
    }

    private Permission getPermission(String resource, String action) {
        for (Permission p : permissions.values()) {
            if (p.getResource().equals(resource) && p.getAction().equals(action)) {
                return p;
            }
        }
        return null;
    }

    public Optional<Role> getRoleById(Long id) {
        return Optional.ofNullable(roles.get(id));
    }

    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    public boolean hasPermission(User user, String resource, String action) {
        if (user == null || user.getRoleId() == null) return false;
        Role role = roles.get(user.getRoleId());
        if (role == null) return false;
        return role.hasPermission(resource, action);
    }
}

