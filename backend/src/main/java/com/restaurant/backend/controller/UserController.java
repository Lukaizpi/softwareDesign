package com.restaurant.backend.controller;

import com.restaurant.backend.Service.AuthService;
import com.restaurant.backend.Service.RoleService;
import com.restaurant.backend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final AuthService authService;
    private final RoleService roleService;

    public UserController(AuthService authService, RoleService roleService) {
        this.authService = authService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        // In production, filter by merchant/branch based on authenticated user
        return authService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return authService.findById(id)
                .map(user -> {
                    roleService.getRoleById(user.getRoleId()).ifPresent(user::setRole);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        // Check if username exists
        if (authService.findByUsername(req.username).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("error", "Username already exists"));
        }

        User user = authService.createUser(
                req.username,
                req.email,
                req.password,
                req.firstName,
                req.lastName,
                req.roleId != null ? req.roleId : 3L, // Default to Employee
                req.merchantId,
                req.branchId
        );

        roleService.getRoleById(user.getRoleId()).ifPresent(user::setRole);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return authService.findById(id)
                .map(user -> {
                    if (req.firstName != null) user.setFirstName(req.firstName);
                    if (req.lastName != null) user.setLastName(req.lastName);
                    if (req.email != null) user.setEmail(req.email);
                    if (req.roleId != null) {
                        user.setRoleId(req.roleId);
                        roleService.getRoleById(req.roleId).ifPresent(user::setRole);
                    }
                    if (req.active != null) user.setActive(req.active);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // In production, soft delete (set active = false) instead of hard delete
        return authService.findById(id)
                .map(user -> {
                    user.setActive(false);
                    return ResponseEntity.ok(Map.of("message", "User deactivated"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner classes for DTOs
    public static class CreateUserRequest {
        public String username;
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public Long roleId;
        public Long merchantId;
        public Long branchId;
    }

    public static class UpdateUserRequest {
        public String firstName;
        public String lastName;
        public String email;
        public Long roleId;
        public Boolean active;
    }
}
