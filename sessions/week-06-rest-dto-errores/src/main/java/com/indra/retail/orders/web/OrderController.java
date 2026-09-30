package com.indra.retail.orders.web;

import com.indra.retail.orders.dto.CreateOrderRequest;
import com.indra.retail.orders.dto.OrderResponse;
import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = new Order(request.customerId(), request.items(), request.deliveryAddress());
        Order created = orderService.create(order);
        OrderResponse response = new OrderResponse(
                created.getId(),
                created.getStatus(),
                created.getTotalAmount(),
                created.getEstimatedDelivery()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable @Size(min = 36, max = 36, message = "{order.id.size}") String orderId) {
        Order order = orderService.findById(orderId);
        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getEstimatedDelivery()
        );
    }
}
