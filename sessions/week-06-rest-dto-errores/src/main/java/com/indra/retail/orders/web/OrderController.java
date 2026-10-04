package com.indra.retail.orders.web;

import com.indra.retail.orders.model.CreateOrderRequest;
import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.model.OrderResponse;
import com.indra.retail.orders.service.OrderService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest order) {
        OrderResponse created = orderService.create(order);
        return ResponseEntity.status(HttpStatus.OK).body(created);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable String orderId) {
        return orderService.findById(orderId);
    }
}
