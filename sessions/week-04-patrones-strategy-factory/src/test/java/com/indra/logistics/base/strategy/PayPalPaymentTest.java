package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class PayPalPaymentTest {

    private final PayPalPayment strategy = new PayPalPayment();

    @Test
    void methodCode_ReturnsPaypal() {
        assertEquals("PAYPAL", strategy.methodCode());
    }

    @Test
    void calculateFee_AppliesTwoPercent() {
        BigDecimal fee = strategy.calculateFee(new BigDecimal("100.00"));
        assertEquals(new BigDecimal("2.00"), fee);
    }

    @Test
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago con PayPal procesado, comisión de plataforma aplicada.", strategy.confirmationMessage());
    }
}

