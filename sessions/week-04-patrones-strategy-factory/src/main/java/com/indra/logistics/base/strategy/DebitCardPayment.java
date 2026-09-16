package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class DebitCardPayment implements PaymentStrategy {

    private static final BigDecimal TARIFF = BigDecimal.valueOf(0.04);

    @Override
    public String methodCode() {
        return "DEBIT_CARD";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(TARIFF).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago con tarjeta de débito procesado, se aplica comisión bancaria.";
    }
}