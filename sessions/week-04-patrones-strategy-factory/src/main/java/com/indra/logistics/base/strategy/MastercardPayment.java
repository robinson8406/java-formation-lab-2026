package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class MastercardPayment implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "MASTERCARD";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return BigDecimal.ZERO;
        } else {
            BigDecimal tariff = CREDIT_CARD_TARIFF.add(BigDecimal.ZERO);
            return amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    public String confirmationMessage(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return "Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.";
        } else {
            return "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
        }
    }
}
