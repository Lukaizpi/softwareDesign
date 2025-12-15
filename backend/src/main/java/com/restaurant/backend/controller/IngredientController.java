package com.restaurant.backend.controller;

import com.restaurant.backend.Service.IngredientService;
import com.restaurant.backend.model.Ingredient;
import com.restaurant.backend.model.IngredientCategory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    // Ingredient Category endpoints
    @GetMapping("/ingredient-categories")
    public List<IngredientCategory> getAllCategories() {
        return ingredientService.getAllCategories();
    }

    @GetMapping("/ingredient-categories/{id}")
    public ResponseEntity<IngredientCategory> getCategoryById(@PathVariable Long id) {
        return ingredientService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ingredient-categories")
    public IngredientCategory createCategory(@RequestBody CreateCategoryRequest req) {
        return ingredientService.createCategory(req.name, req.description);
    }

    // Ingredient endpoints
    @GetMapping("/ingredients")
    public List<Ingredient> getAllIngredients() {
        return ingredientService.getAllIngredients();
    }

    @GetMapping("/ingredients/category/{categoryId}")
    public List<Ingredient> getIngredientsByCategory(@PathVariable Long categoryId) {
        return ingredientService.getIngredientsByCategory(categoryId);
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        return ingredientService.getIngredientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/ingredients")
    public Ingredient createIngredient(@RequestBody CreateIngredientRequest req) {
        return ingredientService.createIngredient(
                req.name,
                req.unit,
                req.price,
                req.categoryId,
                req.merchantId
        );
    }

    @PutMapping("/ingredients/{id}")
    public ResponseEntity<Ingredient> updateIngredient(@PathVariable Long id,
                                                       @RequestBody UpdateIngredientRequest req) {
        return ingredientService.updateIngredient(
                id,
                req.name,
                req.unit,
                req.price,
                req.available,
                req.stock
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Inner classes for DTOs
    public static class CreateCategoryRequest {
        public String name;
        public String description;
    }

    public static class CreateIngredientRequest {
        public String name;
        public String unit;
        public double price;
        public Long categoryId;
        public Long merchantId;
    }

    public static class UpdateIngredientRequest {
        public String name;
        public String unit;
        public Double price;
        public Boolean available;
        public Double stock;
    }
}
