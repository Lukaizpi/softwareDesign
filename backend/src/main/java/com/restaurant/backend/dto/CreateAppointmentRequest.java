package com.restaurant.backend.dto;

public class CreateAppointmentRequest {
    public Long customerId;
    public Long serviceId;

    // ISO-8601, ejemplo: "2025-12-13T10:30:00Z"
    public String appointmentAt;

    public String employeeName;
    public String notes;
}