package com.restaurant.backend.Service;

import com.restaurant.backend.model.User;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AuthService {
    private final Map<String, User> usersByUsername = new HashMap<>();
    private final Map<Long, User> usersById = new HashMap<>();
    private final AtomicLong userIdSeq = new AtomicLong(1);
    private final RoleService roleService;

    public AuthService(RoleService roleService) {
        this.roleService = roleService;
        // Initialize with default admin user
        initializeDefaultUsers();
    }

    private void initializeDefaultUsers() {
        // Admin user - SuperAdmin role (ID 1)
        User admin = new User();
        admin.setId(userIdSeq.getAndIncrement());
        admin.setUsername("admin");
        admin.setEmail("admin@restaurant.com");
        admin.setPassword("admin"); // Simple password for demo
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setActive(true);
        admin.setRoleId(1L); // SuperAdmin role
        admin.setCreatedAt(java.time.Instant.now());
        usersByUsername.put(admin.getUsername(), admin);
        usersById.put(admin.getId(), admin);

        // Manager user - Manager role (ID 2)
        User manager = new User();
        manager.setId(userIdSeq.getAndIncrement());
        manager.setUsername("manager");
        manager.setEmail("manager@restaurant.com");
        manager.setPassword("manager"); // Simple password for demo
        manager.setFirstName("Manager");
        manager.setLastName("User");
        manager.setActive(true);
        manager.setRoleId(2L); // Manager role
        manager.setCreatedAt(java.time.Instant.now());
        usersByUsername.put(manager.getUsername(), manager);
        usersById.put(manager.getId(), manager);

        // Employee user - Employee role (ID 3)
        User employee = new User();
        employee.setId(userIdSeq.getAndIncrement());
        employee.setUsername("employee");
        employee.setEmail("employee@restaurant.com");
        employee.setPassword("employee"); // Simple password for demo
        employee.setFirstName("Employee");
        employee.setLastName("User");
        employee.setActive(true);
        employee.setRoleId(3L); // Employee role
        employee.setCreatedAt(java.time.Instant.now());
        usersByUsername.put(employee.getUsername(), employee);
        usersById.put(employee.getId(), employee);
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(usersById.values());
    }

    public User createUser(String username, String email, String password, String firstName, 
                           String lastName, Long roleId, Long merchantId, Long branchId) {
        User user = new User();
        user.setId(userIdSeq.getAndIncrement());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password); // Should be hashed
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoleId(roleId);
        user.setMerchantId(merchantId);
        user.setBranchId(branchId);
        user.setActive(true);
        user.setCreatedAt(java.time.Instant.now());
        
        usersByUsername.put(username, user);
        usersById.put(user.getId(), user);
        return user;
    }

    public boolean validatePassword(String rawPassword, String hashedPassword) {
        // In real app, use BCryptPasswordEncoder
        return true; // Simplified for now
    }
}

