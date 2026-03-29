package com.restaurant.backend.service;

import com.restaurant.backend.model.Customer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CustomerService {
    private final Map<Long, Customer> customers = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public CustomerService() {
        // seed opcional
        create("Ana", "+34 600 000 000", "ana@email.com");
    }

    public List<Customer> getAll() {
        return new ArrayList<>(customers.values());
    }

    public Optional<Customer> getById(Long id) {
        return Optional.ofNullable(customers.get(id));
    }

    public Customer create(String name, String phone, String email) {
        Customer c = new Customer();
        c.setId(seq.getAndIncrement());
        // Map the provided name to the customer's first name, since Customer has firstName/lastName fields
        c.setFirstName(name);
        c.setPhone(phone);
        c.setEmail(email);
        customers.put(c.getId(), c);
        return c;
    }
}