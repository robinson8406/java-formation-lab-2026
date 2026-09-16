package com.indra.logistics.base.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CashPayment")
class CashPaymentTest {

    private final CashPayment strategy = new CashPayment();

    @Test
    @DisplayName("El código de método debe ser CASH")
    void methodCode_ReturnsCash() {
        assertEquals("CASH", strategy.methodCode());
    }

    @Test
    @DisplayName("La comisión siempre debe ser cero, sin importar el monto")
    void calculateFee_IsAlwaysZero() {
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("200.00")));
        assertEquals(new BigDecimal("0.00"), strategy.calculateFee(new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("El mensaje de confirmación debe indicar que no hay comisión")
    void confirmationMessage_ReturnsExpectedMessage() {
        assertEquals("Pago en efectivo registrado, sin comisión.", strategy.confirmationMessage());
    }
}