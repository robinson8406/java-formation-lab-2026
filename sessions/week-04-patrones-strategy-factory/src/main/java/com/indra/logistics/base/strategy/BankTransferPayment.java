package com.indra.logistics.base.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class BankTransferPayment implements PaymentStrategy {

    @Override
    public String methodCode() {
        return "BANK_TRANSFER";
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(0.025)).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String confirmationMessage(BigDecimal amount) {
        return "Pago por transferencia bancaria registrado, comisión bancaria aplicada.";
    }
}
