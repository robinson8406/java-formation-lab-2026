package com.indra.logistics.base.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.util.Constants;

public class VisaPaymentStrategy implements PaymentStrategy {

    private String confirmationMessage = "Payment confirmed using Visa.";

    @Override
    public String methodCode() {
        return "VISA";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee;
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            fee = BigDecimal.ZERO;
            confirmationMessage = "Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.";
        } else {
            BigDecimal tariff = Constants.CREDIT_TARIFF.add(BigDecimal.valueOf(0.005));
            fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
            confirmationMessage = "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.";
        }
        return fee;
    }

    @Override
    public String confirmationMessage() {
        return confirmationMessage;
    }

}
