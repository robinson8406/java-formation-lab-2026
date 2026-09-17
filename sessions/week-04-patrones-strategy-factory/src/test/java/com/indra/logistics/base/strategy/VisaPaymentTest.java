package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class VisaPaymentTest {

    private final VisaPayment strategy = new VisaPayment();

    @Test
    void methodCode_ReturnsVisa() {
        assertEquals("VISA", strategy.methodCode());
    }

    @Test
    void calculateFee_AmountLessThan100_NoFee() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("50.00"));
        assertEquals(new BigDecimal("0.00"), fee);
        assertEquals("Pago con tarjeta de crédito Visa procesado, monto no aplica comisión bancaria.",
                strategy.confirmationMessage());
    }

    @Test
    void calculateFee_AmountGreaterOrEqual100_AppliesFee() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("7.00"), fee);
        assertEquals("Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}

