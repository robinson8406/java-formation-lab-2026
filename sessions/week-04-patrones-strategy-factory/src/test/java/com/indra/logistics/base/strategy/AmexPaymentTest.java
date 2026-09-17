package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class AmexPaymentTest {

    private final AmexPayment strategy = new AmexPayment();

    @Test
    void methodCode_ReturnsAmex() {
        assertEquals("AMEX", strategy.methodCode());
    }

    @Test
    void calculateFee_AmountLessThan100_NoFee() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("99.99"));
        assertEquals(new BigDecimal("0.00"), fee);
        assertEquals("Pago con tarjeta American Express procesado, monto no aplica comisión bancaria.",
                strategy.confirmationMessage());
    }

    @Test
    void calculateFee_AmountGreaterOrEqual100_AppliesFee() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("6.00"), fee);
        assertEquals("Pago con tarjeta American Express procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}

