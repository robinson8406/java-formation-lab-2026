package com.acme.audit.order.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.acme.audit.order.usecases.OrderApplicationServiceUseCase;
import com.acme.audit.order.application.CreateOrderCommand;

@RestController
@RequestMapping("/orders")
public class OrderController {
	private final OrderApplicationServiceUseCase service;

	public OrderController(OrderApplicationServiceUseCase s) {
		service = s;
	}

	@PostMapping
	public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest r) {
		var v = service.create(new CreateOrderCommand(r.orderId(), r.amount(), r.currency()));
		var status = v.paymentId() == null ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
		return ResponseEntity.status(status).body(new OrderResponse(v.orderId(), v.status(), v.paymentId()));
	}

	public record CreateOrderRequest(@NotBlank @Size(max = 80) String orderId, @NotNull @Positive BigDecimal amount,
			@NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {
	}

	public record OrderResponse(String orderId, String status, String paymentId) {
	}
}