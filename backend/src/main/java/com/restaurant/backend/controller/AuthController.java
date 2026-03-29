package com.restaurant.backend.controller;

import com.restaurant.backend.Service.AuthService;
import com.restaurant.backend.Service.RoleService;
import com.restaurant.backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final RoleService roleService;

    public AuthController(AuthService authService, RoleService roleService) {
        this.authService = authService;
        this.roleService = roleService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.username == null || request.password == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Username and password are required"));
        }

        Optional<User> userOpt = authService.findByUsername(request.username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        
        // Simplified password validation (for demo - should use BCrypt in production)
        // Compare plain text passwords for demo purposes
        if (!user.getPassword().equals(request.password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        
        if (!user.isActive()) {
            return ResponseEntity.status(401).body(Map.of("error", "User is inactive"));
        }

        // Get role information
        roleService.getRoleById(user.getRoleId()).ifPresent(user::setRole);

        // Generate simple token (in production, use JWT)
        String token = "demo-token-" + user.getId() + "-" + System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "firstName", user.getFirstName() != null ? user.getFirstName() : "",
            "lastName", user.getLastName() != null ? user.getLastName() : "",
            "role", user.getRole() != null ? user.getRole().getName() : "Employee",
            "roleId", user.getRoleId()
        ));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Check if username already exists
        if (authService.findByUsername(request.username).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("error", "Username already exists"));
        }

        // Create new user (default to Employee role if not specified)
        Long roleId = request.roleId != null ? request.roleId : 3L; // Employee role
        
        User user = authService.createUser(
            request.username,
            request.email,
            request.password, // Should be hashed in production
            request.firstName,
            request.lastName,
            roleId,
            request.merchantId,
            request.branchId
        );

        roleService.getRoleById(user.getRoleId()).ifPresent(user::setRole);

        Map<String, Object> response = new HashMap<>();
        response.put("user", Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole() != null ? user.getRole().getName() : "Employee"
        ));

        return ResponseEntity.ok(response);
    }

    // Inner classes for request DTOs
    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public Long roleId;
        public Long merchantId;
        public Long branchId;
    }
}

