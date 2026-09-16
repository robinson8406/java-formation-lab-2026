package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class PaypalPayment implements PaymentStrategy {

    private static final BigDecimal TARIFF = BigDecimal.valueOf(0.02);

    @Override
    public String methodCode() {
        return "PAYPAL";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(TARIFF).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago con PayPal procesado, comisión de plataforma aplicada.";
    }
}