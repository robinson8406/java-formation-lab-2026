package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class PayPalPayment implements PaymentStrategy {

    @Override
    public String methodCode()
    {
        return "PAYPAL";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount)
    {
        return amount.multiply(BigDecimal.valueOf(0.02)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage()
    {
        return "Pago con PayPal procesado, comisión de plataforma aplicada.";
    }
}
