package com.example.orders.controller;

import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.model.Order;
import com.example.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest) {
       Order order = orderService.createOrder(orderRequest);
        return ResponseEntity.ok("Order created successfully , your Order ID is " + order.getId());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {

        OrderResponse orderResponse = orderService.getOrder(orderId);
        return ResponseEntity.ok(orderResponse);
    }
}
