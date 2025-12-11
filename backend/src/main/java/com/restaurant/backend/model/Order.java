package com.restaurant.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private Long id;
    private String tableNumber;          // o "tableName", "room", etc.
    private String employeeName;        // quién crea la comanda
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Long id, String tableNumber, String employeeName) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.employeeName = employeeName;
        this.status = OrderStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }
}