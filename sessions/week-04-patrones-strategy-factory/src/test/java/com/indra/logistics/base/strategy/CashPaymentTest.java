package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class CashPaymentTest {

    private final CashPayment strategy = new CashPayment();

    @Test
    void methodCode_ReturnsCash() {
        assertEquals("CASH", strategy.methodCode());
    }

    @Test
    void calculateFee_ReturnsZero() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("0.00"), fee);
    }

    @Test
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago en efectivo registrado, sin comisión.", strategy.confirmationMessage());
    }
}

