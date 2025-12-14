package com.restaurant.backend.model;

import java.time.Instant;

public class Appointment {
    private Long id;
    private Instant bookedAt;
    private Instant appointmentAt;

    private String employeeName;

    private Customer customer;
    private ServiceItem service;

    private AppointmentStatus status = AppointmentStatus.BOOKED;
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Instant getBookedAt() { return bookedAt; }
    public void setBookedAt(Instant bookedAt) { this.bookedAt = bookedAt; }

    public Instant getAppointmentAt() { return appointmentAt; }
    public void setAppointmentAt(Instant appointmentAt) { this.appointmentAt = appointmentAt; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public ServiceItem getService() { return service; }
    public void setService(ServiceItem service) { this.service = service; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}