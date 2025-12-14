package com.restaurant.backend.controller;

import com.restaurant.backend.dto.CreateAppointmentRequest;
import com.restaurant.backend.dto.UpdateAppointmentRequest;
import com.restaurant.backend.model.Appointment;
import com.restaurant.backend.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable Long id) {
        return appointmentService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Appointment> create(@RequestBody CreateAppointmentRequest req) {
        Instant at;
        try {
            at = Instant.parse(req.appointmentAt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        return appointmentService.create(req.customerId, req.serviceId, at, req.employeeName, req.notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable Long id, @RequestBody UpdateAppointmentRequest req) {
        Instant at = null;
        if (req.appointmentAt != null) {
            try {
                at = Instant.parse(req.appointmentAt);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        return appointmentService.update(id, at, req.employeeName, req.notes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        return appointmentService.cancel(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}