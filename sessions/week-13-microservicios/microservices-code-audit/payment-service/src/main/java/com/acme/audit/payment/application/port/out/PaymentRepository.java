package com.acme.audit.payment.application.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import com.acme.audit.payment.domain.PaymentResult;

public interface PaymentRepository {
    Optional<StoredPayment> findByIdempotencyKey(String idempotencyKey);
    PaymentResult save(String idempotencyKey, String orderId, BigDecimal amount, String currency, String status);
    long count();

    record StoredPayment(PaymentResult result, String orderId, BigDecimal amount, String currency) { }
}
