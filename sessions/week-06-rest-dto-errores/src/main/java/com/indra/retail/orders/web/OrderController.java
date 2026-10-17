package com.indra.retail.orders.web;

import com.indra.retail.orders.service.OrderService;
import com.indra.retail.orders.web.dto.CreateOrderRequest;
import com.indra.retail.orders.web.dto.OrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Solo traduce HTTP ↔ DTO y delega en {@link OrderService}. La entidad de dominio
 * no aparece en ninguna firma: entra un {@link CreateOrderRequest} y sale un {@link OrderResponse}.
 */
@RestController
@Validated
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = OrderResponse.from(orderService.create(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderId}")
                .buildAndExpand(response.orderId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getById(
            @PathVariable @Size(max = 36, message = "{order.id.max-size}") String orderId) {
        return OrderResponse.from(orderService.findById(orderId));
    }
}
