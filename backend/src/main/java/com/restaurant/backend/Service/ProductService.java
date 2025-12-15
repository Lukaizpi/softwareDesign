package com.restaurant.backend.Service;

import com.restaurant.backend.model.Product;
import com.restaurant.backend.model.ProductType;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final Map<Long, Product> products = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public ProductService(TaxService taxService) {
        Long food10   = taxService.getByCode("FOOD_10").orElseThrow().getId();
        Long drink10  = taxService.getByCode("DRINK_10").orElseThrow().getId();
        Long alcohol21= taxService.getByCode("ALCOHOL_21").orElseThrow().getId();

        create("Black Coffee", "drink", 1.5, drink10);
        create("Toast with Tomato", "food", 2.8, food10);
        create("Beer", "drink", 3.0, alcohol21);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    public Optional<Product> getProductById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product create(String name, String category, double price, Long taxId) {
        Product p = new Product(seq.getAndIncrement(), name, category, price, taxId);
        products.put(p.getId(), p);
        return p;
    }

    public Product createProduct(String name, ProductType type, double basePrice,
                                String description, Long taxId, Long merchantId,
                                List<Long> ingredientIds) {
        Product product = new Product();
        product.setId(seq.getAndIncrement());
        product.setName(name);
        product.setType(type);
        product.setBasePrice(basePrice);
        product.setTaxId(taxId);
        product.setMerchantId(merchantId);
        product.setIngredientIds(ingredientIds != null ? ingredientIds : new ArrayList<>());
        product.setAvailable(true);
        products.put(product.getId(), product);
        return product;
    }

    public Optional<Product> updateProduct(Long id, String name, ProductType type,
                                          Double basePrice, String description, Boolean available,
                                          Long taxId, List<Long> ingredientIds) {
        Product product = products.get(id);
        if (product == null) return Optional.empty();

        if (name != null) product.setName(name);
        if (type != null) product.setType(type);
        if (basePrice != null) product.setBasePrice(basePrice);
        if (available != null) product.setAvailable(available);
        if (taxId != null) product.setTaxId(taxId);
        if (ingredientIds != null) product.setIngredientIds(ingredientIds);

        return Optional.of(product);
    }
}