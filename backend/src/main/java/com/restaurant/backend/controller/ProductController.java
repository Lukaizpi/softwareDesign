package com.restaurant.backend.controller;

import com.restaurant.backend.model.Product;
import com.restaurant.backend.model.ProductType;
import com.restaurant.backend.Service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody CreateProductRequest req) {
        try {
            Product product = productService.createProduct(
                    req.name,
                    req.type != null ? ProductType.valueOf(req.type) : ProductType.FOOD,
                    req.basePrice != null ? req.basePrice : 0.0,
                    req.description,
                    req.taxId,
                    req.merchantId,
                    req.ingredientIds
            );
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest req) {
        return productService.updateProduct(
                id,
                req.name,
                req.type != null ? ProductType.valueOf(req.type) : null,
                req.basePrice,
                req.description,
                req.available,
                req.taxId,
                req.ingredientIds
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner classes for DTOs
    public static class CreateProductRequest {
        public String name;
        public String type; // "FOOD", "DRINK", "SERVICE"
        public Double basePrice;
        public String description;
        public Long taxId;
        public Long merchantId;
        public java.util.List<Long> ingredientIds;
    }

    public static class UpdateProductRequest {
        public String name;
        public String type;
        public Double basePrice;
        public String description;
        public Boolean available;
        public Long taxId;
        public java.util.List<Long> ingredientIds;
    }
}