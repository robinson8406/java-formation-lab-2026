package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DebitCardPaymentTest {

    private final DebitCardPayment strategy = new DebitCardPayment();

    @Test
    void methodCode_ReturnsDebitCard() {
        assertEquals("DEBIT_CARD", strategy.methodCode());
    }

    @Test
    void calculateFee_AppliesFourPercent() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("4.00"), fee);
    }

    @Test
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago con tarjeta de débito procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}

