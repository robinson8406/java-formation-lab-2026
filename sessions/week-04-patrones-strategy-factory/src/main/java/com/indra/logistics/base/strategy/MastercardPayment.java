package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class MastercardPayment implements PaymentStrategy, ThresholdWaivable {

    private static final BigDecimal TARIFF = BigDecimal.valueOf(0.03);
    private static final BigDecimal THRESHOLD = BigDecimal.valueOf(100);

    @Override
    public String methodCode() {
        return "MASTERCARD";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(TARIFF).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
    }

    @Override
    public BigDecimal threshold() {
        return THRESHOLD;
    }

    @Override
    public String waivedMessage() {
        return "Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.";
    }
}