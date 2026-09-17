package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class VisaPayment implements PaymentStrategy {

    @Override
    public String methodCode()
    {
        return "VISA";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount)
    {
        return amount.multiply(BigDecimal.valueOf(0.035)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage()
    {
        return "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.";
    }
}
