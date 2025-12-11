package com.restaurant.backend.controller; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.restaurant.backend.Service.OrderService;
import com.restaurant.backend.model.Order;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // para que luego React pueda llamar sin problemas
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /api/orders -> lista todas las órdenes
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/orders  (body: { "tableNumber": "5", "employeeName": "Juan" })
    @PostMapping
    public Order createOrder(@RequestBody Map<String, String> request) {
        String tableNumber = request.get("tableNumber");
        String employeeName = request.get("employeeName");
        return orderService.createOrder(tableNumber, employeeName);
    }

    // POST /api/orders/{id}/items  (body: { "productId": 1, "quantity": 2 })
    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Long productId = Long.valueOf(request.get("productId").toString());
        int quantity = Integer.parseInt(request.get("quantity").toString());

        return orderService.addItemToOrder(id, productId, quantity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // POST /api/orders/{id}/cancel
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // POST /api/orders/{id}/pay
    @PostMapping("/{id}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable Long id) {
        return orderService.markAsPaid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}