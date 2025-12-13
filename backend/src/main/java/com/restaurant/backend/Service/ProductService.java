package com.restaurant.backend.service;

import com.restaurant.backend.model.Product;

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

        create("Café solo", "drink", 1.5, drink10);
        create("Tostada con tomate", "food", 2.8, food10);
        create("Cerveza", "drink", 3.0, alcohol21);
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
}