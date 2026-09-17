package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class BankTransferPaymentTest {

    private final BankTransferPayment strategy = new BankTransferPayment();

    @Test
    void methodCode_ReturnsBankTransfer() {
        assertEquals("BANK_TRANSFER", strategy.methodCode());
    }

    @Test
    void calculateFee_AppliesTwoPointFivePercent() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("2.50"), fee);
    }

    @Test
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago por transferencia bancaria registrado, comisión bancaria aplicada.",
                strategy.confirmationMessage());
    }
}

