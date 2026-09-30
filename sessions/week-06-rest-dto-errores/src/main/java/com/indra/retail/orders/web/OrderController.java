package com.indra.retail.orders.web;

import com.indra.retail.orders.model.Order;
import com.indra.retail.orders.service.OrderService;
import com.indra.retail.orders.web.dto.CreateOrderRequest;
import com.indra.retail.orders.web.dto.OrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderService.create(orderMapper.toOrder(request));
        URI location = URI.create("/api/orders/" + created.getId());
        return ResponseEntity.created(location).body(orderMapper.toResponse(created));
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(@PathVariable @NotBlank(message = "{order.orderId.notBlank}") String orderId) {
        return orderMapper.toResponse(orderService.findById(orderId));
    }
}
