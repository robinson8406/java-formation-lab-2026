package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.domain.PaymentMethod;

/**
 * Contrato Strategy: cada método de pago sabe calcular su propia comisión y
 * mensaje.
 */
public class PaypalPayment implements PaymentStrategy {

    @Override
    public PaymentMethod methodCode() {
        return PaymentMethod.PAYPAL;
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
