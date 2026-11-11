package com.acme.audit.order.application.port.out;

import java.math.BigDecimal;

public interface OrderStore {
    OrderData getOrCreate(String orderId, BigDecimal amount, String currency);
    OrderData confirm(String orderId, String paymentId);
    OrderData markPaymentUnavailable(String orderId);

    record OrderData(String orderId, String idempotencyKey, BigDecimal amount, String currency,
                     String status, String paymentId) { }
}
