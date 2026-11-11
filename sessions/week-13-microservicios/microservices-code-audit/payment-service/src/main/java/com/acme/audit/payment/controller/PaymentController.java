package com.acme.audit.payment.controller;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.audit.payment.domain.PaymentResult;
import com.acme.audit.payment.application.CreatePaymentCommand;
import com.acme.audit.payment.usecases.PaymentApplicationServiceUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/payments")
@Validated
public class PaymentController {
	private final PaymentApplicationServiceUseCase service;

	public PaymentController(PaymentApplicationServiceUseCase service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PaymentResponse> create(@RequestHeader("Idempotency-Key") @NotBlank @Size(max = 150) String key,
			@Valid @RequestBody CreatePaymentRequest request) {
		PaymentResult result = service.process(key, new CreatePaymentCommand(request.orderId(), request.amount(), request.currency()));
		
				
		return ResponseEntity.status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
				.body(new PaymentResponse(result.paymentId(), result.orderId(), result.status(), result.replayed()));
	}

	public record CreatePaymentRequest(@NotBlank @Size(max = 80) String orderId, @NotNull @Positive BigDecimal amount,
			@NotBlank @Pattern(regexp = "[A-Z]{3}") String currency) {
	}

	public record PaymentResponse(String paymentId, String orderId, String status, boolean replayed) {
	}
}