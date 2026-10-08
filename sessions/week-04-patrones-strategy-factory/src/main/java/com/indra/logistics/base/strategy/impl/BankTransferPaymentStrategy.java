package com.indra.logistics.base.strategy.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.indra.logistics.base.strategy.PaymentStrategy;

public class BankTransferPaymentStrategy implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "BANK_TRANSFER";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.025)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage() {
        return "Pago por transferencia bancaria registrado, comisión bancaria aplicada.";
    }

}
