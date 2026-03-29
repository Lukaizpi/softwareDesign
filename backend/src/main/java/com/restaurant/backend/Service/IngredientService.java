package com.restaurant.backend.Service;

import com.restaurant.backend.model.Ingredient;
import com.restaurant.backend.model.IngredientCategory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IngredientService {
    private final Map<Long, Ingredient> ingredients = new HashMap<>();
    private final Map<Long, IngredientCategory> categories = new HashMap<>();
    private final AtomicLong ingredientSeq = new AtomicLong(1);
    private final AtomicLong categorySeq = new AtomicLong(1);

    public IngredientService() {
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Create sample categories
        IngredientCategory vegetables = createCategory("Vegetables", "Fresh vegetables");
        IngredientCategory proteins = createCategory("Proteins", "Meat and protein sources");
        IngredientCategory dairy = createCategory("Dairy", "Dairy products");

        // Create sample ingredients
        createIngredient("Tomato", "kg", 2.5, vegetables.getId(), null);
        createIngredient("Lettuce", "kg", 1.8, vegetables.getId(), null);
        createIngredient("Chicken", "kg", 8.0, proteins.getId(), null);
        createIngredient("Milk", "liter", 1.2, dairy.getId(), null);
    }

    // Category Management
    public List<IngredientCategory> getAllCategories() {
        return new ArrayList<>(categories.values());
    }

    public Optional<IngredientCategory> getCategoryById(Long id) {
        return Optional.ofNullable(categories.get(id));
    }

    public IngredientCategory createCategory(String name, String description) {
        IngredientCategory category = new IngredientCategory();
        category.setId(categorySeq.getAndIncrement());
        category.setName(name);
        category.setDescription(description);
        categories.put(category.getId(), category);
        return category;
    }

    // Ingredient Management
    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients.values());
    }

    public List<Ingredient> getIngredientsByCategory(Long categoryId) {
        return ingredients.values().stream()
                .filter(i -> i.getCategoryId() != null && i.getCategoryId().equals(categoryId))
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<Ingredient> getIngredientById(Long id) {
        return Optional.ofNullable(ingredients.get(id));
    }

    public Ingredient createIngredient(String name, String unit, double price, Long categoryId, Long merchantId) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientSeq.getAndIncrement());
        ingredient.setName(name);
        ingredient.setUnit(unit);
        ingredient.setPrice(price);
        ingredient.setCategoryId(categoryId);
        ingredient.setMerchantId(merchantId);
        ingredient.setAvailable(true);
        ingredients.put(ingredient.getId(), ingredient);
        return ingredient;
    }

    public Optional<Ingredient> updateIngredient(Long id, String name, String unit, Double price, Boolean available, Double stock) {
        Ingredient ingredient = ingredients.get(id);
        if (ingredient == null) return Optional.empty();

        if (name != null) ingredient.setName(name);
        if (unit != null) ingredient.setUnit(unit);
        if (price != null) ingredient.setPrice(price);
        if (available != null) ingredient.setAvailable(available);
        if (stock != null) ingredient.setStock(stock);

        return Optional.of(ingredient);
    }

    public double calculateIngredientCost(List<Long> ingredientIds, Map<Long, Double> quantities) {
        double totalCost = 0.0;
        for (Long ingredientId : ingredientIds) {
            Ingredient ingredient = ingredients.get(ingredientId);
            if (ingredient != null && ingredient.isAvailable()) {
                double quantity = quantities.getOrDefault(ingredientId, 1.0);
                totalCost += ingredient.getPrice() * quantity;
            }
        }
        return totalCost;
    }
}
