package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AmexPayment")
class AmexPaymentTest {

    private final AmexPayment strategy = new AmexPayment();

    @Test
    @DisplayName("El código de método debe ser AMEX")
    void methodCode_ReturnsAmex() {
        assertEquals("AMEX", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión debe ser el 3% del monto")
    void calculateFee_AppliesThreePercent() {
        assertEquals(new BigDecimal("6.00"), strategy.calculateFee(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("El mensaje debe indicar que se aplicó comisión bancaria")
    void confirmationMessage_ReturnsCommissionAppliedMessage() {
        assertEquals(
                "Pago con tarjeta American Express procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}