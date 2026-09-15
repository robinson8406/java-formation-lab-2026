package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.indra.logistics.base.domain.PaymentMethod;

@Component
public class CashPayment implements PaymentStrategy {

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.CASH;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago en efectivo registrado, sin comisión.";
    }
}
