package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class MasterCardPayment implements PaymentStrategy {

    @Override
    public String methodCode()
    {
        return "MASTERCARD";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount)
    {
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage()
    {
        return "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
    }
}
