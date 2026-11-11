package com.acme.audit.order.application.port.out;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentOutcome charge(String idempotencyKey, String orderId, BigDecimal amount, String currency);

    record PaymentOutcome(String paymentId, String status, boolean replayed) { }
}
