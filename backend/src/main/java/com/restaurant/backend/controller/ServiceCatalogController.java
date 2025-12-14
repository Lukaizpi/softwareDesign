package com.restaurant.backend.controller;

import com.restaurant.backend.model.ServiceItem;
import com.restaurant.backend.service.ServiceCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceCatalogController(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @GetMapping
    public List<ServiceItem> getAll() {
        return serviceCatalogService.getAll();
    }

    @PostMapping
    public ServiceItem create(@RequestBody Map<String, Object> req) {
        String name = req.get("name").toString();
        int duration = Integer.parseInt(req.get("durationMinutes").toString());
        double price = Double.parseDouble(req.get("price").toString());
        return serviceCatalogService.create(name, duration, price);
    }
}