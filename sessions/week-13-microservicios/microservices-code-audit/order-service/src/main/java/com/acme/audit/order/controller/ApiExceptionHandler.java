package com.acme.audit.order.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(com.acme.audit.order.persistence.OrderConflictException.class)
    public ResponseEntity<Map<String, Object>> orderConflict(com.acme.audit.order.persistence.OrderConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(), "status", 409,
                "code", "ORDER_ID_REUSED", "message", exception.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.Conflict.class)
    public ResponseEntity<Map<String, Object>> paymentConflict(HttpClientErrorException.Conflict exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "timestamp", Instant.now().toString(), "status", 409,
                "code", "PAYMENT_REQUEST_CONFLICT", "message", "Payment rejected the idempotency key reuse"));
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> paymentUnavailable(RestClientException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 503,
                "code", "PAYMENT_SERVICE_UNAVAILABLE",
                "message", "Payment service did not complete the request"));
    }
}
