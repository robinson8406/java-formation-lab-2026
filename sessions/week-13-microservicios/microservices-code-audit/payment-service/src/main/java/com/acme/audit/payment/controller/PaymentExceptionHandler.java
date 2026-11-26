package com.acme.audit.payment.controller;

import com.acme.audit.payment.usecases.PaymentApplicationServiceUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(PaymentApplicationServiceUseCase.IdempotencyKeyConflictException.class)
    ResponseEntity<Map<String, Object>> idempotencyConflict(PaymentApplicationServiceUseCase.IdempotencyKeyConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 409,
                "code", "IDEMPOTENCY_KEY_REUSED",
                "message", exception.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    ResponseEntity<Map<String, Object>> providerIdempotencyConflict(HttpClientErrorException.Conflict exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(), "status", 409,
                "code", "IDEMPOTENCY_KEY_REUSED",
                "message", "Provider rejected reuse of an idempotency key with a different payload"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    ResponseEntity<Map<String, Object>> providerUnavailable(ResourceAccessException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 503,
                "code", "PAYMENT_PROVIDER_UNAVAILABLE",
                "message", "Payment provider did not respond within the configured time"));
    }
}
