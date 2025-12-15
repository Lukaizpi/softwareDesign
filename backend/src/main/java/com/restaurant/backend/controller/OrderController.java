package com.restaurant.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.backend.model.Order;
import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.createOrder(req.tableNumber, req.employeeName));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItem(@PathVariable Long id, @RequestBody AddItemRequest req) {
        return orderService.addItemToOrder(id, req.productId, req.quantity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id) {
        return orderService.markAsPaid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        return orderService.updateStatus(id, req.status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/discount")
    public ResponseEntity<Order> applyDiscount(@PathVariable Long id, @RequestBody ApplyDiscountRequest req) {
        return orderService.applyDiscount(id, req.amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Order> addPayment(@PathVariable Long id, @RequestBody AddPaymentRequest req) {
        return orderService.addPayment(id, req.method, req.amount, req.tip)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Order> refund(@PathVariable Long id, @RequestBody RefundRequest req) {
        return orderService.refund(id, req.amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // Split Payment Endpoints
    @PostMapping("/{id}/split/start")
    public ResponseEntity<Order> startSplitPayment(@PathVariable Long id) {
        return orderService.startSplitPayment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/split")
    public ResponseEntity<Order> addPaymentSplit(@PathVariable Long id, @RequestBody SplitPaymentRequest req) {
        double tip = req.tip >= 0 ? req.tip : 0.0;
        return orderService.addPaymentSplit(
                id,
                req.customerName,
                req.method,
                req.amount,
                tip,
                req.itemIds
        )
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{id}/split/complete")
    public ResponseEntity<Order> completeSplitPayment(@PathVariable Long id) {
        return orderService.completeSplitPayment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}