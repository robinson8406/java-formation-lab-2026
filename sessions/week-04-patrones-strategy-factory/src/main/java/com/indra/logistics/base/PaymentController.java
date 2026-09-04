package com.indra.logistics.base;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PaymentController {

    private final PaymentService paymentService = new PaymentService();

    @GetMapping("/payments/{paymentMethod}")
    public PaymentResult getFee(@PathVariable String paymentMethod) {
        return getFee(paymentMethod, BigDecimal.valueOf(100));
    }

    @GetMapping("/payments/{paymentMethod}/{amount}")
    public PaymentResult getFee(@PathVariable String paymentMethod, @PathVariable BigDecimal amount) {
        return paymentService.process(new PaymentRequest(amount, paymentMethod));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleUnknownMethod(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
