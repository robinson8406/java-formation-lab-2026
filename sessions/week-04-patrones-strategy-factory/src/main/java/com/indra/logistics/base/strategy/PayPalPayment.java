package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class PayPalPayment implements PaymentStrategy {

    private static final String METHOD_CODE = "PAYPAL";
    private static final BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.02);

    @Override
    public String methodCode() {
        return METHOD_CODE;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago con PayPal procesado, comisión de plataforma aplicada.";
    }
}
