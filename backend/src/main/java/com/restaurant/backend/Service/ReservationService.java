package com.restaurant.backend.Service;

import com.restaurant.backend.model.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final Map<Long, Reservation> reservations = new HashMap<>();
    private final Map<Long, Customer> customers = new HashMap<>();
    private final Map<Long, com.restaurant.backend.model.Service> services = new HashMap<>();
    private final Map<Long, Schedule> schedules = new HashMap<>();
    private final AtomicLong reservationSeq = new AtomicLong(1);
    private final AtomicLong customerSeq = new AtomicLong(1);
    private final AtomicLong serviceSeq = new AtomicLong(1);
    private final AtomicLong scheduleSeq = new AtomicLong(1);
    private final ProductService productService;

    public ReservationService(ProductService productService) {
        this.productService = productService;
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Sample customer
        Customer customer = new Customer();
        customer.setId(customerSeq.getAndIncrement());
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");
        customer.setPhone("123456789");
        customers.put(customer.getId(), customer);
    }

    // Customer Management
    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    public Optional<Customer> getCustomerById(Long id) {
        return Optional.ofNullable(customers.get(id));
    }

    public Customer createCustomer(String firstName, String lastName, String email, String phone, String notes) {
        Customer customer = new Customer();
        customer.setId(customerSeq.getAndIncrement());
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setNotes(notes);
        customer.setCreatedAt(java.time.Instant.now());
        customers.put(customer.getId(), customer);
        return customer;
    }

    // Service Management
    public List<com.restaurant.backend.model.Service> getAllServices() {
        return new ArrayList<>(services.values());
    }

    public Optional<com.restaurant.backend.model.Service> getServiceById(Long id) {
        return Optional.ofNullable(services.get(id));
    }

    public com.restaurant.backend.model.Service createService(String name, String description, int durationMinutes, double price, Long merchantId, Long branchId) {
        com.restaurant.backend.model.Service service = new com.restaurant.backend.model.Service();
        service.setId(serviceSeq.getAndIncrement());
        service.setName(name);
        service.setDescription(description);
        service.setDurationMinutes(durationMinutes);
        service.setPrice(price);
        service.setMerchantId(merchantId);
        service.setBranchId(branchId);
        service.setActive(true);
        services.put(service.getId(), service);
        return service;
    }

    // Reservation Management
    public List<Reservation> getAllReservations() {
        return reservations.values().stream()
                .map(this::enrichReservation)
                .collect(Collectors.toList());
    }

    public Optional<Reservation> getReservationById(Long id) {
        return Optional.ofNullable(reservations.get(id)).map(this::enrichReservation);
    }

    public Reservation createReservation(Long customerId, Long serviceId, Long employeeId, Long branchId,
                                       LocalDateTime startTime, String notes, double depositAmount) {
        // Try to get Service first, if not found, try Product of type SERVICE
        com.restaurant.backend.model.Service service = getServiceById(serviceId).orElse(null);
        int duration = 30; // Default duration
        
        if (service == null) {
            // Try to get as Product of type SERVICE
            Product product = productService.getProductById(serviceId).orElse(null);
            if (product != null && (product.getType() == ProductType.SERVICE || 
                (product.getCategory() != null && product.getCategory().equalsIgnoreCase("service")))) {
                // Use product as service, default duration 30 minutes
                duration = 30; // Default duration for service products
            } else {
                throw new IllegalArgumentException("Service not found");
            }
        } else {
            duration = service.getDurationMinutes();
        }

        // Check availability
        if (!isAvailable(serviceId, employeeId, startTime, duration)) {
            throw new IllegalArgumentException("Time slot not available");
        }

        LocalDateTime endTime = startTime.plusMinutes(duration);

        Reservation reservation = new Reservation();
        reservation.setId(reservationSeq.getAndIncrement());
        reservation.setCustomerId(customerId);
        reservation.setServiceId(serviceId);
        reservation.setEmployeeId(employeeId);
        reservation.setBranchId(branchId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setNotes(notes);
        reservation.setDepositAmount(depositAmount);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setCreatedAt(java.time.Instant.now());
        reservation.setUpdatedAt(java.time.Instant.now());

        reservations.put(reservation.getId(), reservation);
        return enrichReservation(reservation);
    }

    public Optional<Reservation> updateReservation(Long id, LocalDateTime startTime, String notes, Long employeeId) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) return Optional.empty();

        if (startTime != null) {
            com.restaurant.backend.model.Service service = getServiceById(reservation.getServiceId()).orElse(null);
            if (service != null) {
                if (!isAvailable(reservation.getServiceId(), employeeId != null ? employeeId : reservation.getEmployeeId(),
                        startTime, service.getDurationMinutes(), id)) {
                    return Optional.empty(); // Not available
                }
                reservation.setStartTime(startTime);
                reservation.setEndTime(startTime.plusMinutes(service.getDurationMinutes()));
            }
        }

        if (notes != null) reservation.setNotes(notes);
        if (employeeId != null) reservation.setEmployeeId(employeeId);
        reservation.setUpdatedAt(java.time.Instant.now());

        return Optional.of(enrichReservation(reservation));
    }

    public Optional<Reservation> cancelReservation(Long id) {
        Reservation reservation = reservations.get(id);
        if (reservation == null) return Optional.empty();

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
            reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            return Optional.empty();
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(java.time.Instant.now());

        // Calculate cancellation fee if needed
        LocalDateTime now = LocalDateTime.now();
        if (reservation.getStartTime().isAfter(now) && 
            reservation.getStartTime().minusHours(24).isBefore(now)) {
            reservation.setCancellationFee(reservation.getService().getPrice() * 0.2); // 20% fee
        }

        return Optional.of(enrichReservation(reservation));
    }

    public List<String> getAvailableSlots(Long serviceId, Long employeeId, LocalDateTime date) {
        // Try to get Service first, if not found, try Product of type SERVICE
        com.restaurant.backend.model.Service service = getServiceById(serviceId).orElse(null);
        int duration = 30; // Default duration
        
        if (service == null) {
            // Try to get as Product of type SERVICE
            Product product = productService.getProductById(serviceId).orElse(null);
            if (product == null) {
                return Collections.emptyList();
            }
            
            // Check if it's a SERVICE type product
            // First check the type enum
            boolean isServiceProduct = (product.getType() != null && product.getType() == ProductType.SERVICE);
            
            // Fallback: check category (for legacy products)
            if (!isServiceProduct && product.getCategory() != null) {
                isServiceProduct = product.getCategory().equalsIgnoreCase("service");
            }
            
            // Last resort: check if name contains "service"
            if (!isServiceProduct && product.getName() != null) {
                isServiceProduct = product.getName().toLowerCase().contains("service");
            }
            
            if (!isServiceProduct) {
                // Product found but not a SERVICE type - return empty
                return Collections.emptyList();
            }
            // Use product as service, default duration 30 minutes
            duration = 30;
        } else {
            duration = service.getDurationMinutes();
        }

        List<String> slots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0); // 9 AM
        LocalTime endTime = LocalTime.of(18, 0); // 6 PM

        LocalTime current = startTime;
        LocalDateTime now = LocalDateTime.now();
        LocalDate selectedDate = date.toLocalDate();
        LocalDate today = now.toLocalDate();
        
        // For future dates, show all slots. For today, only show future slots
        boolean isFutureDate = selectedDate.isAfter(today);
        
        while (current.plusMinutes(duration).isBefore(endTime) || current.plusMinutes(duration).equals(endTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(selectedDate, current);
            
            // Check if slot is in the future
            boolean isFutureSlot = isFutureDate || slotDateTime.isAfter(now);
            
            if (isFutureSlot && isAvailable(serviceId, employeeId, slotDateTime, duration)) {
                slots.add(current.toString());
            }
            current = current.plusMinutes(30); // 30-minute intervals
        }

        return slots;
    }

    private boolean isAvailable(Long serviceId, Long employeeId, LocalDateTime startTime, int durationMinutes) {
        return isAvailable(serviceId, employeeId, startTime, durationMinutes, null);
    }

    private boolean isAvailable(Long serviceId, Long employeeId, LocalDateTime startTime, int durationMinutes, Long excludeReservationId) {
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        LocalDateTime now = LocalDateTime.now();

        // Check if time is in the past (already checked in getAvailableSlots, but double-check)
        if (startTime.isBefore(now)) {
            return false;
        }

        // Check existing reservations for conflicts
        for (Reservation res : reservations.values()) {
            if (excludeReservationId != null && res.getId().equals(excludeReservationId)) {
                continue;
            }
            if (res.getStatus() == Reservation.ReservationStatus.CANCELLED ||
                res.getStatus() == Reservation.ReservationStatus.COMPLETED) {
                continue;
            }
            // If employee is specified, only check reservations for that employee
            if (employeeId != null && res.getEmployeeId() != null && !res.getEmployeeId().equals(employeeId)) {
                continue;
            }

            // Check for overlap - only check if reservation is for the same service
            if (res.getServiceId() != null && res.getServiceId().equals(serviceId)) {
                if (startTime.isBefore(res.getEndTime()) && endTime.isAfter(res.getStartTime())) {
                    return false; // Overlapping reservation
                }
            }
        }

        // If employee is specified, check their schedule
        if (employeeId != null) {
            DayOfWeek dayOfWeek = startTime.getDayOfWeek();
            boolean hasSchedule = schedules.values().stream()
                    .anyMatch(s -> s.getEmployeeId().equals(employeeId) &&
                            s.getDayOfWeek() == dayOfWeek &&
                            s.isAvailable());
            
            if (hasSchedule) {
                // Check if slot is within scheduled hours
                boolean withinSchedule = schedules.values().stream()
                        .anyMatch(s -> s.getEmployeeId().equals(employeeId) &&
                                s.getDayOfWeek() == dayOfWeek &&
                                s.isAvailable() &&
                                startTime.toLocalTime().isAfter(s.getStartTime()) &&
                                endTime.toLocalTime().isBefore(s.getEndTime()));
                return withinSchedule; // Must be within scheduled hours
            }
            // No schedule defined, allow it (flexible scheduling)
        }

        // Available if no conflicts
        return true;
    }

    private Reservation enrichReservation(Reservation reservation) {
        if (reservation.getCustomerId() != null) {
            getCustomerById(reservation.getCustomerId()).ifPresent(reservation::setCustomer);
        }
        if (reservation.getServiceId() != null) {
            // Try to get Service first
            Optional<com.restaurant.backend.model.Service> service = getServiceById(reservation.getServiceId());
            if (service.isPresent()) {
                reservation.setService(service.get());
            } else {
                // Try to get as Product of type SERVICE
                Product product = productService.getProductById(reservation.getServiceId()).orElse(null);
                if (product != null && (product.getType() == ProductType.SERVICE || 
                    (product.getCategory() != null && product.getCategory().equalsIgnoreCase("service")))) {
                    // Convert Product to Service for display
                    com.restaurant.backend.model.Service serviceFromProduct = new com.restaurant.backend.model.Service();
                    serviceFromProduct.setId(product.getId());
                    serviceFromProduct.setName(product.getName());
                    serviceFromProduct.setPrice(product.getBasePrice());
                    serviceFromProduct.setDurationMinutes(30); // Default
                    reservation.setService(serviceFromProduct);
                }
            }
        }
        return reservation;
    }

    public Schedule createSchedule(Long employeeId, Long branchId, DayOfWeek dayOfWeek,
                                   LocalTime startTime, LocalTime endTime) {
        Schedule schedule = new Schedule();
        schedule.setId(scheduleSeq.getAndIncrement());
        schedule.setEmployeeId(employeeId);
        schedule.setBranchId(branchId);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setAvailable(true);
        schedules.put(schedule.getId(), schedule);
        return schedule;
    }
}
