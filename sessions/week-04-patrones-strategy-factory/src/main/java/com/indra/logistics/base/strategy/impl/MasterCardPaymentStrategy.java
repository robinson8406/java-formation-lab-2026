package com.indra.logistics.base.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;
import com.indra.logistics.base.util.Constants;

public class MasterCardPaymentStrategy implements PaymentStrategy {

    private String confirmationMessage = "Payment confirmed using MasterCard.";

    @Override
    public String methodCode() {
        return "MASTER_CARD";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        BigDecimal fee;
        if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
            fee = BigDecimal.ZERO;
            confirmationMessage = "Pago con tarjeta de crédito Mastercard procesado, monto no aplica comisión bancaria.";
        } else {
            BigDecimal tariff = Constants.CREDIT_TARIFF.add(BigDecimal.ZERO);
            fee = amount.multiply(tariff).setScale(2, RoundingMode.HALF_UP);
            confirmationMessage = "Pago con tarjeta de crédito Mastercard procesado, se aplica comisión bancaria.";
        }
        return fee;
    }

    @Override
    public String confirmationMessage() {
        return confirmationMessage;
    }

}
