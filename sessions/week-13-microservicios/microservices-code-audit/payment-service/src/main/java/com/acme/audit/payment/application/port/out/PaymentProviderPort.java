package com.acme.audit.payment.application.port.out;

import java.math.BigDecimal;

public interface PaymentProviderPort {
    ProviderCharge charge(String idempotencyKey, String orderId, BigDecimal amount, String currency);

    record ProviderCharge(String providerTransactionId, String status) { }
}
