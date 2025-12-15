package com.restaurant.backend.Service;

import com.restaurant.backend.model.Tax;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaxService {

    private final Map<Long, Tax> taxes = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public TaxService() {
        // seeds
        create("FOOD_10", "IVA comida", 0.10);
        create("DRINK_10", "IVA bebidas", 0.10);
        create("ALCOHOL_21", "IVA alcohol", 0.21);
    }

    public List<Tax> getAll() {
        return new ArrayList<>(taxes.values());
    }

    public Optional<Tax> getById(Long id) {
        return Optional.ofNullable(taxes.get(id));
    }

    public Tax create(String code, String name, double rate) {
        Tax t = new Tax();
        t.setId(seq.getAndIncrement());
        t.setCode(code);
        t.setName(name);
        t.setRate(rate);
        t.setActive(true);
        taxes.put(t.getId(), t);
        return t;
    }

    public Optional<Tax> update(Long id, String code, String name, double rate, Boolean active) {
        Tax t = taxes.get(id);
        if (t == null) return Optional.empty();
        if (code != null) t.setCode(code);
        if (name != null) t.setName(name);
        if (rate >= 0) t.setRate(rate);
        if (active != null) t.setActive(active);
        return Optional.of(t);
    }

    public Optional<Tax> delete(Long id) {
        Tax t = taxes.remove(id);
        return Optional.ofNullable(t);
    }

    public Optional<Tax> getByCode(String code) {
    return taxes.values().stream()
            .filter(t -> t.getCode().equalsIgnoreCase(code))
            .findFirst();
}
}