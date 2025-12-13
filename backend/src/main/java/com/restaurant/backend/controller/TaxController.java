package com.restaurant.backend.controller;

import com.restaurant.backend.model.Tax;
import com.restaurant.backend.service.TaxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/taxes")
@CrossOrigin(origins = "*")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @GetMapping
    public java.util.List<Tax> getAll() {
        return taxService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tax> getById(@PathVariable Long id) {
        return taxService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Tax create(@RequestBody Map<String, Object> req) {
        String code = req.get("code").toString();
        String name = req.get("name").toString();
        double rate = Double.parseDouble(req.get("rate").toString());
        return taxService.create(code, name, rate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tax> update(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String code = req.containsKey("code") ? req.get("code").toString() : null;
        String name = req.containsKey("name") ? req.get("name").toString() : null;
        double rate = req.containsKey("rate") ? Double.parseDouble(req.get("rate").toString()) : -1;
        Boolean active = req.containsKey("active") ? Boolean.valueOf(req.get("active").toString()) : null;

        return taxService.update(id, code, name, rate, active)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Tax> delete(@PathVariable Long id) {
        return taxService.delete(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}