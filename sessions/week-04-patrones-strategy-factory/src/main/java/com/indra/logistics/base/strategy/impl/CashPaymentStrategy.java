package com.indra.logistics.base.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;

public class CashPaymentStrategy implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "CASH";
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
