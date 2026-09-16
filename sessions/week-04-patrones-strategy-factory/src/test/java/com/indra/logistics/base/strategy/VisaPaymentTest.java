package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VisaPayment")
class VisaPaymentTest {

    private final VisaPayment strategy = new VisaPayment();

    @Test
    @DisplayName("El código de método debe ser VISA")
    void methodCode_ReturnsVisa() {
        assertEquals("VISA", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión debe ser el 3.5% del monto")
    void calculateFee_AppliesThreePointFivePercent() {
        assertEquals(new BigDecimal("7.00"), strategy.calculateFee(new BigDecimal("200.00")));
    }

    @Test
    @DisplayName("El mensaje debe indicar que se aplicó comisión bancaria")
    void confirmationMessage_ReturnsCommissionAppliedMessage() {
        assertEquals(
                "Pago con tarjeta de crédito Visa procesado, se aplica comisión bancaria.",
                strategy.confirmationMessage());
    }
}