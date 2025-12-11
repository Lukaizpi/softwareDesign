package com.restaurant.backend.Service;

import com.restaurant.backend.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        // Datos de ejemplo en memoria
        products.add(new Product(1L, "Café solo", "drink", 1.50, 0.10));
        products.add(new Product(2L, "Tostada con tomate", "food", 2.80, 0.10));
        products.add(new Product(3L, "Cerveza", "drink", 3.00, 0.21));
    }

    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(products);
    }
}