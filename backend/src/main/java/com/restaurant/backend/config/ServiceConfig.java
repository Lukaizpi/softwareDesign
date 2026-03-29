package com.restaurant.backend.config;

import com.restaurant.backend.Service.IngredientService;
import com.restaurant.backend.Service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class ServiceConfig {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private IngredientService ingredientService;

    @PostConstruct
    public void init() {
        // Resolve circular dependency
        // PricingService already has IngredientService injected via constructor
    }
}

