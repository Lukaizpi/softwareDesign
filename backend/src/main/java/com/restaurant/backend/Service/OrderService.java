package com.restaurant.backend.Service; 


import org.springframework.stereotype.Service;

import com.restaurant.backend.model.Order;
import com.restaurant.backend.model.OrderItem;
import com.restaurant.backend.model.OrderStatus;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new HashMap<>();
    private final AtomicLong orderIdGenerator = new AtomicLong(1);
    private final AtomicLong orderItemIdGenerator = new AtomicLong(1);

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Optional<Order> getOrderById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public Order createOrder(String tableNumber, String employeeName) {
        Long id = orderIdGenerator.getAndIncrement();
        Order order = new Order(id, tableNumber, employeeName);
        orders.put(id, order);
        return order;
    }

    public Optional<Order> addItemToOrder(Long orderId, Long productId, int quantity) {
        Order order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderStatus.OPEN) {
            return Optional.empty();
        }

        Long itemId = orderItemIdGenerator.getAndIncrement();
        OrderItem item = new OrderItem(itemId, productId, quantity);
        order.addItem(item);
        return Optional.of(order);
    }

    public Optional<Order> cancelOrder(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderStatus.OPEN) {
            return Optional.empty();
        }
        order.setStatus(OrderStatus.CANCELLED);
        return Optional.of(order);
    }

    public Optional<Order> markAsPaid(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderStatus.OPEN) {
            return Optional.empty();
        }
        order.setStatus(OrderStatus.PAID);
        return Optional.of(order);
    }
}