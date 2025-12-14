package com.restaurant.backend.service;

import com.restaurant.backend.model.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AppointmentService {

    private final Map<Long, Appointment> appointments = new LinkedHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    private final CustomerService customerService;
    private final ServiceCatalogService serviceCatalogService;

    public AppointmentService(CustomerService customerService, ServiceCatalogService serviceCatalogService) {
        this.customerService = customerService;
        this.serviceCatalogService = serviceCatalogService;
    }

    public List<Appointment> getAll() {
        return new ArrayList<>(appointments.values());
    }

    public Optional<Appointment> getById(Long id) {
        return Optional.ofNullable(appointments.get(id));
    }

    public Optional<Appointment> create(Long customerId, Long serviceId, Instant appointmentAt, String employeeName, String notes) {
        if (appointmentAt == null) return Optional.empty();
        if (employeeName == null || employeeName.isBlank()) return Optional.empty();

        Customer c = customerService.getById(customerId).orElse(null);
        if (c == null) return Optional.empty();

        ServiceItem s = serviceCatalogService.getById(serviceId).orElse(null);
        if (s == null) return Optional.empty();

        Appointment a = new Appointment();
        a.setId(seq.getAndIncrement());
        a.setBookedAt(Instant.now());
        a.setAppointmentAt(appointmentAt);
        a.setEmployeeName(employeeName);
        a.setCustomer(c);
        a.setService(s);
        a.setStatus(AppointmentStatus.BOOKED);
        a.setNotes(notes);

        appointments.put(a.getId(), a);
        return Optional.of(a);
    }

    public Optional<Appointment> update(Long id, Instant appointmentAt, String employeeName, String notes) {
        Appointment a = appointments.get(id);
        if (a == null) return Optional.empty();

        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            return Optional.empty();
        }

        if (appointmentAt != null) a.setAppointmentAt(appointmentAt);
        if (employeeName != null && !employeeName.isBlank()) a.setEmployeeName(employeeName);
        if (notes != null) a.setNotes(notes);

        return Optional.of(a);
    }

    public Optional<Appointment> cancel(Long id) {
        Appointment a = appointments.get(id);
        if (a == null) return Optional.empty();
        if (a.getStatus() != AppointmentStatus.BOOKED) return Optional.empty();

        a.setStatus(AppointmentStatus.CANCELLED);
        return Optional.of(a);
    }
}