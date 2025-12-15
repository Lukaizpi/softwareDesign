package com.restaurant.backend.controller;

import com.restaurant.backend.Service.ReservationService;
import com.restaurant.backend.model.Reservation;
import com.restaurant.backend.model.Customer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return reservationService.getReservationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest req) {
        try {
            Reservation reservation = reservationService.createReservation(
                    req.customerId,
                    req.serviceId,
                    req.employeeId,
                    req.branchId,
                    LocalDateTime.parse(req.startTime),
                    req.notes,
                    req.depositAmount != null ? req.depositAmount : 0.0
            );
            return ResponseEntity.ok(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(@PathVariable Long id,
                                                         @RequestBody UpdateReservationRequest req) {
        return reservationService.updateReservation(
                id,
                req.startTime != null ? LocalDateTime.parse(req.startTime) : null,
                req.notes,
                req.employeeId
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/availability")
    public ResponseEntity<List<String>> getAvailableSlots(
            @RequestParam Long serviceId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<String> slots = reservationService.getAvailableSlots(serviceId, employeeId, date);
        return ResponseEntity.ok(slots);
    }

    // Customer endpoints (moved to separate endpoints for clarity)
    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return reservationService.getAllCustomers();
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody CreateCustomerRequest req) {
        return reservationService.createCustomer(
                req.firstName,
                req.lastName,
                req.email,
                req.phone,
                req.notes
        );
    }

    // Service endpoints
    @GetMapping("/services")
    public List<com.restaurant.backend.model.Service> getAllServices() {
        return reservationService.getAllServices();
    }

    // Inner classes for DTOs
    public static class CreateReservationRequest {
        public Long customerId;
        public Long serviceId;
        public Long employeeId;
        public Long branchId;
        public String startTime;
        public String notes;
        public Double depositAmount;
    }

    public static class UpdateReservationRequest {
        public String startTime;
        public String notes;
        public Long employeeId;
    }

    public static class CreateCustomerRequest {
        public String firstName;
        public String lastName;
        public String email;
        public String phone;
        public String notes;
    }
}
