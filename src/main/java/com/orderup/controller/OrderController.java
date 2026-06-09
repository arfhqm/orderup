package com.orderup.controller;

import com.orderup.dto.OrderRequest;
import com.orderup.dto.PaymentRequest;
import com.orderup.entity.Order;
import com.orderup.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest req) {
        return ResponseEntity.ok(orderService.placeOrder(req));
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> editOrder(@PathVariable Long id, @RequestBody OrderRequest req) {
        return ResponseEntity.ok(orderService.editOrder(id, req));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Order> payOrder(@PathVariable Long id, @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(orderService.payOrder(id, req));
    }
}
