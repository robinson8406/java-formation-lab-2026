package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class VisaPayment implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "VISA";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal tariff = CREDIT_CARD_TARIFF.add(BigDecimal.valueOf(0.005));
            return amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String confirmationMessage(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return "Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.";
        } else {
            return "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.";
        }
    }
}
