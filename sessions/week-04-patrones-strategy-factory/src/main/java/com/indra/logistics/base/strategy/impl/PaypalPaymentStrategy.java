package com.indra.logistics.base.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;

public class PaypalPaymentStrategy implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "PAYPAL";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.02)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago con PayPal procesado, comisión de plataforma aplicada.";
    }

}
