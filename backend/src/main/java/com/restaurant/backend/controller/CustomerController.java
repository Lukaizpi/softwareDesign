package com.restaurant.backend.controller;

import com.restaurant.backend.model.Customer;
import com.restaurant.backend.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> getAll() {
        return customerService.getAll();
    }

    @PostMapping
    public Customer create(@RequestBody Map<String, String> req) {
        return customerService.create(req.get("name"), req.get("phone"), req.get("email"));
    }
}