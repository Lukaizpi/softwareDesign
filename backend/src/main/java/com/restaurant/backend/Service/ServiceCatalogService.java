package com.restaurant.backend.service;

import com.restaurant.backend.model.ServiceItem;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ServiceCatalogService {
    private final Map<Long, ServiceItem> services = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public ServiceCatalogService() {
        create("Corte de pelo", 30, 15.0);
        create("Barba", 20, 10.0);
        create("Manicura", 45, 25.0);
    }

    public List<ServiceItem> getAll() {
        return new ArrayList<>(services.values());
    }

    public Optional<ServiceItem> getById(Long id) {
        return Optional.ofNullable(services.get(id));
    }

    public ServiceItem create(String name, int durationMinutes, double price) {
        ServiceItem s = new ServiceItem();
        s.setId(seq.getAndIncrement());
        s.setName(name);
        s.setDurationMinutes(durationMinutes);
        s.setPrice(price);
        services.put(s.getId(), s);
        return s;
    }
}